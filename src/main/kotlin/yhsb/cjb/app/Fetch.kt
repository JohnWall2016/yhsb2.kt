package yhsb.cjb.app

import picocli.CommandLine
import yhsb.base.cmd.CommandWithHelp
import yhsb.base.cmd.ExcelUpdateByPersonCommand
import yhsb.base.excel.*
import yhsb.base.text.fillRight
import yhsb.base.text.insertBeforeLastSubstring
import yhsb.cjb.net.protocol.PersonInfoInProvinceQuery
import yhsb.cjb.net.Session
import yhsb.cjb.net.protocol.PayingInfoQuery
import yhsb.cjb.net.protocol.getPauseInfoByIdCard
import yhsb.cjb.net.protocol.getStopInfoByIdCard
import yhsb.cjb.net.protocol.Result
import java.math.BigDecimal
import java.nio.file.Paths

@CommandLine.Command(
    description = ["城居保信息检索程序"],
    subcommands = [
        Fetch.Query::class,
        Fetch.Update::class,
        Fetch.PayQuery::class
    ]
)
class Fetch : CommandWithHelp() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(Fetch()).execute(*args)
        }
    }

    override fun run() {
        CommandLine.usage(Fetch(), System.out)
    }

    @CommandLine.Command(
        name = "query",
        description = ["个人综合查询"]
    )
    class Query : CommandWithHelp() {
        @CommandLine.Parameters(description = ["身份证号码"])
        private var idCards: Array<String>? = null

        @CommandLine.Option(names = ["-d", "--detail"], description = ["是否显示详细情况"])
        private var detail = false

        override fun run() {
            if (idCards != null) {
                Session.use {
                    idCards?.forEach { idCard ->
                        sendService(PersonInfoInProvinceQuery(idCard))
                        val result = getResult<PersonInfoInProvinceQuery.Item>()
                        if (result.isEmpty()) {
                            println("$idCard 未参保")
                        } else {
                            val p = result.first()
                            println("${p.idCard} ${p.name} ${p.jbState} ${p.dwName} ${p.csName}")
                            if (!detail) {
                                println("${p.idCard} ${p.name} ${p.jbState} ${p.dwName} ${p.csName}")
                            } else {
                                val info = when (p.cbState.value) {
                                    "2" -> { // 暂停参保
                                        getPauseInfoByIdCard(idCard)?.let {
                                            "暂停参保(${it.reason}, ${it.yearMonth}, ${it.memo})"
                                        } ?: ""
                                    }
                                    "4" -> { // 终止参保
                                        getStopInfoByIdCard(idCard)?.let {
                                            "终止参保(${it.reason}, ${it.yearMonth}, ${it.memo})"
                                        } ?: ""
                                    }
                                    else -> ""
                                }
                                println("${p.idCard} ${p.name} ${p.jbState} ${p.dwName} ${p.csName} $info")
                            }
                        }
                    }
                }
            }
        }
    }

    @CommandLine.Command(
        name = "update",
        description = ["更新excel表格中人员参保信息"]
    )
    class Update : ExcelUpdateByPersonCommand() {
        override fun run() {
            println("开始处理数据")

            val workbook = Excel.load(excel)
            val sheet = workbook.getSheetAt(0)

            Session.use {
                for (index in (startRow - 1) until endRow) {
                    val row = sheet.getRow(index)
                    val name = row.getCell(nameCol).getValue()
                    val idCard = row.getCell(idCardCol).getValue().trim().toUpperCase()

                    sendService(PersonInfoInProvinceQuery(idCard))
                    val result = getResult<PersonInfoInProvinceQuery.Item>()
                    val state = if (result.isNotEmpty()) {
                        result.first().jbState
                    } else {
                        "未参保"
                    }

                    println("更新 ${name.fillRight(8)}$idCard $state")
                    row.getOrCreateCell(updateCol).setCellValue(state)
                }
            }

            workbook.save(excel.insertBeforeLastSubstring(".up"))

            println("结束数据处理")
        }
    }

    @CommandLine.Command(
        name = "payQuery",
        description = ["缴费信息查询"]
    )
    class PayQuery : CommandWithHelp() {
        @CommandLine.Option(
            names = ["-e", "--export"],
            description = ["导出信息表"]
        )
        private var export = false

        @CommandLine.Parameters(
            description = ["身份证号码"]
        )
        private var idCard = ""

        private val outputDir = """D:\征缴管理"""
        private val template = """雨湖区城乡居民基本养老保险缴费查询单模板.xlsx"""

        /** 缴费记录 */
        open class PayRecord(
            val year: Int? = null, // 年度
            var personal: BigDecimal = BigDecimal.ZERO, // 个人缴费
            var provincial: BigDecimal = BigDecimal.ZERO, // 省级补贴
            var civic: BigDecimal = BigDecimal.ZERO, // 市级补贴
            var prefectural: BigDecimal = BigDecimal.ZERO, // 县级补贴
            var governmentalPay: BigDecimal = BigDecimal.ZERO, // 政府代缴
            var communalPay: BigDecimal = BigDecimal.ZERO, // 集体补助
            var fishmanPay: BigDecimal = BigDecimal.ZERO, // 退捕渔民补助
            val transferDates: LinkedHashSet<String> = LinkedHashSet(), // 划拨日期
            val agencies: LinkedHashSet<String> = LinkedHashSet() // 社保机构
        )

        /** 缴费合计记录  */
        class PayTotalRecord(
            var total: BigDecimal = BigDecimal.ZERO
        ) : PayRecord()

        data class MapRecord(
            val paidRecords: MutableMap<Int, PayRecord> = mutableMapOf(),
            val unpaidRecords: MutableMap<Int, PayRecord> = mutableMapOf()
        )

        fun Result<PayingInfoQuery.Item>.getRecords(): MapRecord {
            val mapRecord = MapRecord()

            for (data in this) {
                val year = data.year
                if (year != null) {
                    val records = if (data.isPaidOff()) mapRecord.paidRecords else mapRecord.unpaidRecords
                    var record = records[year]
                    if (record == null) {
                       record = PayRecord(year)
                       records[year] = record
                    }
                    val amount = data.amount
                    when (data.item.value) {
                        "1" -> record.personal += amount
                        "3" -> record.provincial += amount
                        "4" -> record.civic += amount
                        "5" -> record.prefectural += amount
                        "6" -> record.communalPay += amount
                        "11" -> record.governmentalPay += amount
                        "15" -> record.fishmanPay += amount
                        else -> throw Exception("未知缴费类型$type, 金额$amount")
                    }
                    record.agencies.add(data.agency)
                    record.transferDates.add(data.paidOffDay ?: "")
                }
            }

            return mapRecord
        }

        fun Map<Int, PayRecord>.orderAndSum(): List<PayRecord> {
            if (isEmpty()) return listOf()
            val result = values.toMutableList()
            result.sortBy { it.year }
            val total = PayTotalRecord()
            result.forEach {
                total.personal += it.personal
                total.provincial += it.provincial
                total.civic += it.civic
                total.prefectural += it.prefectural
                total.governmentalPay += it.governmentalPay
                total.communalPay += it.communalPay
                total.fishmanPay += it.fishmanPay
            }
            total.total =
                total.personal +
                total.provincial + total.civic + total.prefectural +
                total.governmentalPay + total.communalPay + total.fishmanPay
            result.add(total)
            return result
        }

        fun PersonInfoInProvinceQuery.Item.print() {
            println("个人信息:")
            println(String.format("%s %s %s %s %s %s %s", name, idCard,
                jbState, jbKind, agency, czName, opTime))
        }

        fun PayRecord.format(): String {
            return if (this !is PayTotalRecord) {
                String.format("%5s%9s%9s%9s%9s%9s%9s%13s  %s %s", year,
                    personal, provincial, civic, prefectural,
                    governmentalPay, communalPay, fishmanPay,
                    agencies.joinToString("|"),
                    transferDates.joinToString("|"))
            } else {
                String.format(" 合计%9s%9s%9s%9s%9s%9s%13s",
                    personal, provincial, civic, prefectural,
                    governmentalPay, communalPay, fishmanPay) +
                "  总计: $total"
            }
        }

        fun List<PayRecord>.print(message: String) {
            println(message)
            println(String.format("%2s%3s%6s%5s%5s%5s%5s%5s%7s %s %s",
                "序号", "年度", "个人缴费", "省级补贴", "市级补贴", "县级补贴",
                "政府代缴", "集体补助", "退捕渔民补助", "社保经办机构", "划拨时间"))
            var i = 1
            for (r in this) {
                val t = if (r is PayTotalRecord) "" else "${i++}"
                println(String.format("%3s %s", t, r.format()))
            }
        }

        override fun run() {
            val (info, payResult) = Session.use {
                sendService(PersonInfoInProvinceQuery(idCard))
                val result = getResult<PersonInfoInProvinceQuery.Item>()
                if (result.isEmpty()) {
                    Pair(null, null)
                } else {
                    sendService(PayingInfoQuery(idCard))
                    val piResult = getResult<PayingInfoQuery.Item>()
                    Pair(
                        result.first(),
                        if (piResult.isNotEmpty()) piResult else null
                    )
                }
            }

            if (info == null) {
                println("未查到参保记录")
                return
            }
            info.print()

            val (paidRecords, _) = if (payResult == null) {
                println("未查询到缴费信息")
                Pair(null, null)
            } else {
                val (paid, unpaid) = payResult.getRecords()
                val records = paid.orderAndSum()
                val unrecords = unpaid.orderAndSum()
                records.print("\n已拨付缴费历史记录:")
                if (unrecords.isNotEmpty()) {
                    unrecords.print("\n未拨付补录入记录:")
                }
                Pair(records, unrecords)
            }

            if (export) {
                val workbook = Excel.load(Paths.get(outputDir, template))
                val sheet = workbook.getSheetAt(0).apply {
                    getCell("A5").setValue(info.name)
                    getCell("C5").setValue(info.idCard)
                    getCell("E5").setValue(info.agency)
                    getCell("G5").setValue(info.czName)
                    getCell("K5").setValue(info.opTime)
                }

                if (paidRecords != null) {
                    var index = 8
                    val copyIndex = index
                    paidRecords.forEach { record ->
                        sheet.getOrCopyRow(index++, copyIndex, true).apply {
                            if (record is PayTotalRecord) {
                                getCell("A").setValue("")
                                getCell("B").setValue("合计")
                            } else {
                                getCell("A").setValue("${index - copyIndex}")
                                getCell("B").setValue(record.year)
                            }
                            getCell("C").setValue(record.personal)
                            getCell("D").setValue(record.provincial)
                            getCell("E").setValue(record.civic)
                            getCell("F").setValue(record.prefectural)
                            getCell("G").setValue(record.governmentalPay)
                            getCell("H").setValue(record.communalPay)
                            getCell("I").setValue(record.fishmanPay)

                            if (record is PayTotalRecord) {
                                getCell("J").setValue("总计")
                                getCell("L").setValue(record.total)
                            } else {
                                getCell("J").setValue(record.agencies.joinToString("|"))
                                getCell("L").setValue(record.transferDates.joinToString("|"))
                            }
                        }
                    }
                }
                workbook.save(Paths.get(outputDir, "${info.name}缴费查询单.xlsx"))
            }
        }
    }
}
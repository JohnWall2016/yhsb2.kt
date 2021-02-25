package yhsb.cjb.app

import picocli.CommandLine
import yhsb.base.cmd.CommandWithHelp
import yhsb.base.datetime.DateTime
import yhsb.base.excel.*
import yhsb.base.text.fillRight
import yhsb.base.text.insertBeforeLastSubstring
import yhsb.cjb.net.Session
import yhsb.cjb.net.protocol.*
import java.math.BigDecimal
import java.text.Collator
import java.util.*

@CommandLine.Command(
    description = ["代发数据导出制表程序"],
    subcommands = [
        Delegate.PersonList::class,
        Delegate.PaymentList::class
    ]
)
class Delegate : CommandWithHelp() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(Delegate()).execute(*args)
        }
    }

    override fun run() {
        CommandLine.usage(Delegate(), System.out)
    }

    @CommandLine.Command(
        name = "personList",
        description = ["正常代发人员名单导出"]
    )
    class PersonList : CommandWithHelp() {
        @CommandLine.Parameters(
            description = ["代发类型: 801 - 独生子女, 802 - 乡村教师, 803 - 乡村医生, 807 - 电影放映员, 808 - 核工业"]
        )
        private var type = ""

        @CommandLine.Parameters(
            description = ["代发年月: 格式 YYYYMM, 如 201901"]
        )
        private var yearMonth = ""

        @CommandLine.Option(
            names = ["-a", "--all"],
            description = ["导出所有居保正常代发人员"]
        )
        private var all = false

        @CommandLine.Option(
            names = ["-e", "--estimate"],
            description = ["是否测算代发金额"]
        )
        private var estimate = false

        private val template = """D:\代发管理\雨湖区城乡居民基本养老保险代发人员名单.xlsx"""

        override fun run() {
            println("开始导出数据")

            val workbook = Excel.load(template)
            val sheet = workbook.getSheetAt(0)

            val startRow = 3
            var currentRow = startRow

            var sum = BigDecimal.ZERO
            var payedSum = BigDecimal.ZERO

            val date = DateTime.format("yyyyMMdd")
            val dateCh = DateTime.format("yyyy年M月d日")
            sheet.getCell("G2").setValue("制表时间：$dateCh")

            Session.use {
                sendService(DelegatePersonQuery(type, "1", ""))
                val result = getResult<DelegatePersonQuery.Item>()
                result.forEach {
                    if (it.pid == null) return@forEach

                    println("${it.name.fillRight(8)}${it.idCard}")
                    if (!all && it.dfState.value != "1") return@forEach

                    if (it.dfState.value != "1" &&
                        !(it.dfState.value == "2" && it.cbState.value == "1")
                    ) return@forEach

                    var payAmount = BigDecimal.ZERO
                    if (it.standard != null) {
                        var startYear = it.startYearMonth / 100
                        var startMonth = it.startYearMonth % 100
                        startMonth -= 1
                        if (startMonth == 0) {
                            startYear -= 1
                            startMonth = 12
                        }
                        if (it.endYearMonth != null) {
                            startYear = it.endYearMonth / 100
                            startMonth = it.endYearMonth % 100
                        }
                        val m = """^(\d\d\d\d)(\d\d)$""".toRegex().find(yearMonth)
                        if (m != null) {
                            val endYear = m.groupValues[1].toInt()
                            val endMonth = m.groupValues[2].toInt()
                            payAmount =
                                ((endYear - startYear) * 12 + endMonth - startMonth).toBigDecimal() * it.standard
                        }
                    } else if (type == "801" && it.totalPayed?.toInt() == 5000) {
                        return@forEach
                    }

                    sheet.getOrCopyRow(currentRow++, startRow).apply {
                        getCell("A").setValue(currentRow - startRow)
                        getCell("B").setValue(it.csName)
                        getCell("C").setValue(it.name)
                        getCell("D").setValue(it.idCard)
                        getCell("E").setValue(it.startYearMonth)
                        getCell("F").setValue(it.standard)
                        getCell("G").setValue(it.type)
                        getCell("H").setValue(it.dfState.toString())
                        getCell("I").setValue(it.cbState.toString())
                        getCell("J").setValue(it.endYearMonth)
                        getCell("K").setValue(it.totalPayed)

                        payedSum += it.totalPayed ?: BigDecimal.ZERO
                        if (estimate) getCell("L").setValue(payAmount)
                        sum += payAmount
                    }
                }

                sheet.getOrCopyRow(currentRow, startRow).apply {
                    getCell("A").setValue("")
                    getCell("C").setValue("共计")
                    getCell("D").setValue(currentRow - startRow)
                    getCell("E").setValue("")
                    getCell("F").setValue("")
                    getCell("J").setValue("合计")
                    getCell("K").setValue(payedSum)

                    if (estimate) getCell("L").setValue(sum)
                }

                workbook.save(
                    template.insertBeforeLastSubstring(
                        "${DfType(type)}${if (all) "ALL" else ""}$date"
                    )
                )
            }
            println("结束数据导出")
        }
    }

    @CommandLine.Command(
        name = "paymentList",
        description = ["代发支付明细导出"]
    )
    class PaymentList : CommandWithHelp() {
        @CommandLine.Parameters(
            description = [
                "业务类型: DF0001 - 独生子女, DF0002 - 乡村教师, DF0003 - 乡村医生, DF0007 - 电影放映员, DF0008 - 核工业"
            ]
        )
        private var type = ""

        @CommandLine.Parameters(
            description = ["支付年月: 格式 YYYYMM, 如 201901"]
        )
        private var date = ""

        private val template = """D:\代发管理\雨湖区城乡居民基本养老保险代发人员支付明细.xlsx"""

        data class Item(
            val csName: String,
            val name: String,
            val idCard: String,
            val type: String,
            val yearMonth: Int,
            val startDate: Int?,
            val endDate: Int?,
            val amount: BigDecimal,
            val memo: String
        )

        override fun run() {
            val items = mutableListOf<Item>()
            var total = BigDecimal.ZERO
            val payType = DfPayType(type)

            Session.use {
                sendService(DelegatePaymentQuery(type, date))
                val result = getResult<DelegatePaymentQuery.Item>()
                result.forEach { list ->
                    if (list.typeCh != null) {
                        sendService(DelegatePaymentDetailQuery(list.payList))
                        val dpResult = getResult<DelegatePaymentDetailQuery.Item>()
                        dpResult.forEach { detail ->
                            if (detail.csName != null && detail.flag == "0") {
                                sendService(DelegatePaymentPersonalDetailQuery(detail))
                                val dppResult = getResult<DelegatePaymentPersonalDetailQuery.Item>()
                                var startDate: Int? = null
                                var endDate: Int? = null
                                val count = dppResult.size() ?: 0
                                if (count > 0) {
                                    startDate = dppResult.first().date
                                    endDate = if (count > 2) {
                                        dppResult[count - 2]?.date
                                    } else {
                                        startDate
                                    }
                                }
                                total += detail.amount
                                items.add(
                                    Item(
                                        detail.csName,
                                        detail.name,
                                        detail.idCard,
                                        payType.toString(),
                                        detail.yearMonth,
                                        startDate,
                                        endDate,
                                        detail.amount,
                                        list.bankType ?: "未绑定支付账户"
                                    )
                                )
                            }
                        }
                    }
                }
            }

            items.sortWith { e1, e2 ->
                Collator.getInstance(Locale.CHINESE).compare(e1.csName, e2.csName)
            }

            val workbook = Excel.load(template)
            val sheet = workbook.getSheetAt(0)
            val startRow = 3
            var currentRow = startRow
            val date = DateTime.format("yyyyMMdd")
            val dateCh = DateTime.format("yyyy年M月d日")

            sheet.getCell("G2").setValue("制表时间：$dateCh")

            for (item in items) {
                sheet.getOrCopyRow(currentRow++, startRow).apply {
                    getCell("A").setValue(currentRow - startRow)
                    getCell("B").setValue(item.csName)
                    getCell("C").setValue(item.name)
                    getCell("D").setValue(item.idCard)
                    getCell("E").setValue(item.type)
                    getCell("F").setValue(item.yearMonth)
                    getCell("G").setValue(item.startDate)
                    getCell("H").setValue(item.endDate)
                    getCell("I").setValue(item.amount)
                    getCell("J").setValue(item.memo)
                }
            }

            sheet.getOrCopyRow(currentRow, startRow).apply {
                getCell("A").setValue("")
                getCell("C").setValue("共计")
                getCell("D").setValue(currentRow - startRow)
                getCell("F").setValue("")
                getCell("G").setValue("")
                getCell("H").setValue("合计")
                getCell("I").setValue(total)
            }

            workbook.save(template.insertBeforeLastSubstring("($payType)$date"))
        }
    }
}
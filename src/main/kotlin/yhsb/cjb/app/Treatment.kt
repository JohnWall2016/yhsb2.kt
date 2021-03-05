package yhsb.cjb.app

import com.google.common.base.Strings
import picocli.CommandLine
import yhsb.base.cmd.CommandWithHelp
import yhsb.base.datetime.DateTime
import yhsb.base.excel.*
import yhsb.base.text.fillRight
import yhsb.base.text.stripPrefix
import yhsb.base.text.times
import yhsb.cjb.net.Session
import yhsb.cjb.net.protocol.*
import yhsb.cjb.net.protocol.Division.groupByDwAndCsName
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.Collator
import java.util.*

@CommandLine.Command(
    description = ["信息核对报告表和养老金计算表生成程序"],
    subcommands = [
        Treatment.Download::class,
        Treatment.Split::class,
        Treatment.FailedPayList::class
    ]
)
class Treatment : CommandWithHelp() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(Treatment()).execute(*args)
        }
    }

    override fun run() {
        CommandLine.usage(Treatment(), System.out)
    }

    @CommandLine.Command(
        name = "download",
        description = ["从业务系统下载信息核对报告表"]
    )
    class Download : CommandWithHelp() {
        @CommandLine.Parameters(
            description = ["报表生成日期, 格式: YYYYMMDD, 例如: 20210101"]
        )
        private var date = ""

        private val outputDir = """D:\待遇核定"""

        private val template = "信息核对报告表模板.xlsx"

        override fun run() {
            val result = Session.use {
                sendService(TreatmentReviewQuery(reviewState = "0"))
                getResult<TreatmentReviewQuery.Item>()
            }

            val workbook = Excel.load(Paths.get(outputDir, template))
            val sheet = workbook.getSheetAt(0)
            val startRow = 3
            var currentRow = 3

            result.forEach {
                val index = currentRow - startRow + 1

                println("$index ${it.idCard} ${it.name} ${it.bz ?: ""}")

                sheet.getOrCopyRow(currentRow++, startRow).apply {
                    getCell("A").setValue(index)
                    getCell("B").setValue(it.name)
                    getCell("C").setValue(it.idCard)
                    getCell("D").setValue(it.xzqh)
                    getCell("E").setValue(it.payAmount)
                    getCell("F").setValue(it.payMonth)
                    getCell("G").setValue("是 [ ]")
                    getCell("H").setValue("否 [ ]")
                    getCell("I").setValue("是 [ ]")
                    getCell("J").setValue("否 [ ]")
                    getCell("L").setValue(it.bz)
                }
                workbook.save(Paths.get(outputDir, "信息核对报告表$date.xlsx"))
            }
        }
    }

    @CommandLine.Command(
        name = "split",
        description = ["对下载的信息表分组并生成养老金计算表"]
    )
    class Split : CommandWithHelp() {
        @CommandLine.Parameters(
            description = ["报表生成日期, 格式: YYYYMMDD, 例如: 20210101"]
        )
        private var date = ""

        @CommandLine.Parameters(
            paramLabel = "startRow",
            description = ["开始行(从1开始)"]
        )
        private var startRow = 0

        @CommandLine.Parameters(
            paramLabel = "endRow",
            description = ["结束行(包含在内)"]
        )
        private var endRow = 0

        private val rootDir = """D:\待遇核定"""

        private val template = "养老金计算表模板.xlsx"

        override fun run() {
            val (year, month) = DateTime.split(date)

            val inputExcel = Paths.get(rootDir, "信息核对报告表$date.xlsx")
            val infoExcel = Paths.get(rootDir, "信息核对报告表模板.xlsx")
            val outputDir = Paths.get(rootDir, "${year}年${month.stripPrefix("0")}月待遇核定数据")

            val workbook = Excel.load(inputExcel)
            val sheet = workbook.getSheetAt(0)

            println("生成分组映射表")
            val map = iterator {
                for (index in (startRow - 1) until endRow) {
                    yield(Pair(sheet.getCell(index, "D").getValue(), index))
                }
            }.groupByDwAndCsName()

            println("生成分组目录并分别生成信息核对报告表")
            if (Files.exists(outputDir)) {
                Files.move(outputDir, Paths.get("$outputDir.orig"))
            }
            Files.createDirectory(outputDir)

            map.keys.forEach { dw ->
                println("$dw:")
                Files.createDirectory(outputDir.resolve(dw))

                map[dw]?.keys?.forEach { cs ->
                    println("  $cs: ${map[dw]?.get(cs)}")
                    Files.createDirectory(outputDir.resolve(Paths.get(dw, cs)))

                    val outWorkbook = Excel.load(infoExcel)
                    val outSheet = outWorkbook.getSheetAt(0)
                    val startRow = 3
                    var currentRow = 3

                    map[dw]?.get(cs)?.forEach { rowIndex ->
                        val index = currentRow - startRow + 1
                        val inRow = sheet.getRow(rowIndex)

                        println("    $index ${inRow.getCell("C").getValue()} ${inRow.getCell("B").getValue()}")

                        outSheet.getOrCopyRow(currentRow++, startRow).apply {
                            getCell("A").setValue(index)
                            inRow.copyTo(this, "B", "C", "D", "E", "F", "G", "H", "I", "J", "L")
                        }
                    }

                    outWorkbook.save(outputDir.resolve(Paths.get(dw, cs, "${cs}信息核对报告表.xlsx")))
                }
            }

            println("\n按分组生成养老金养老金计算表")
            Session.use {
                map.keys.forEach { dw ->
                    map[dw]?.keys?.forEach { cs ->
                        map[dw]?.get(cs)?.forEach { index ->
                            val row = sheet.getRow(index)
                            val name = row.getCell("B").getValue()
                            val idCard = row.getCell("C").getValue()
                            println("  $idCard $name")

                            try {
                                getPaymentInfoReport(name, idCard, outputDir.resolve(Paths.get(dw, cs)))
                            } catch (e: Exception) {
                                println("$idCard $name 获得养老金计算表岀错: $e")
                            }
                        }
                    }
                }
            }
        }

        fun Session.getPaymentInfoReport(name: String, idCard: String, outDir: Path, retry: Int = 3) {
            var retry = retry
            sendService(TreatmentReviewQuery(idCard, "0"))
            val result = getResult<TreatmentReviewQuery.Item>()
            if (result.isNotEmpty()) {
                sendService(BankInfoQuery(idCard))
                val biResult = getResult<BankInfoQuery.Item>()
                var payInfo = result.first().getTreatmentInfoMatch()
                while (payInfo == null) {
                    if (--retry > 0) {
                        payInfo = result.first().getTreatmentInfoMatch()
                    } else {
                        throw Exception("养老金计算信息无效")
                    }
                }
                val workbook = Excel.load(Paths.get(rootDir, template))
                workbook.getSheetAt(0).apply {
                    getCell("A5").setValue(payInfo.groupValues[1])
                    getCell("B5").setValue(payInfo.groupValues[2])
                    getCell("C5").setValue(payInfo.groupValues[3])
                    getCell("F5").setValue(payInfo.groupValues[4])
                    getCell("I5").setValue(payInfo.groupValues[5])
                    getCell("L5").setValue(payInfo.groupValues[6])
                    getCell("A8").setValue(payInfo.groupValues[7])
                    getCell("B8").setValue(payInfo.groupValues[8])
                    getCell("C8").setValue(payInfo.groupValues[9])
                    getCell("E8").setValue(payInfo.groupValues[10])
                    getCell("F8").setValue(payInfo.groupValues[11])
                    getCell("G8").setValue(payInfo.groupValues[12])
                    getCell("H8").setValue(payInfo.groupValues[13])
                    getCell("I8").setValue(payInfo.groupValues[14])
                    getCell("J8").setValue(payInfo.groupValues[15])
                    getCell("K8").setValue(payInfo.groupValues[16])
                    getCell("L8").setValue(payInfo.groupValues[17])
                    getCell("M8").setValue(payInfo.groupValues[18])
                    getCell("N8").setValue(payInfo.groupValues[19])
                    getCell("A11").setValue(payInfo.groupValues[20])
                    getCell("B11").setValue(payInfo.groupValues[21])
                    getCell("C11").setValue(payInfo.groupValues[22])
                    getCell("D11").setValue(payInfo.groupValues[23])
                    getCell("E11").setValue(payInfo.groupValues[24])
                    getCell("F11").setValue(payInfo.groupValues[25])
                    getCell("G11").setValue(payInfo.groupValues[26])
                    getCell("H11").setValue(payInfo.groupValues[27])
                    getCell("I11").setValue(payInfo.groupValues[28])
                    getCell("J11").setValue(payInfo.groupValues[29])
                    getCell("K11").setValue(payInfo.groupValues[30])
                    getCell("L11").setValue(payInfo.groupValues[31])
                    getCell("M11").setValue(payInfo.groupValues[32])
                    getCell("N11").setValue(payInfo.groupValues[33])
                    getCell("I12").setValue(DateTime.format("yyyy-MM-dd HH:mm:ss"))

                    if (biResult.isNotEmpty()) {
                        biResult.first().let {
                            getCell("B15").setValue(it.countName)
                            getCell("F15").setValue(it.bankType.name)

                            var card = it.cardNumber
                            val len = card.length
                            if (len > 7) {
                                card = card.substring(0, 3) + "*".times(len - 7) + card.substring(len - 4)
                            } else if (len > 4) {
                                card = "*".times(len - 4) + card.substring(len - 4)
                            }
                            getCell("J15").setValue(card)
                        }
                    } else {
                        getCell("B15").setValue("未绑定银行账户")
                    }
                }
                workbook.save(outDir.resolve("${name}[$idCard]养老金计算表.xlsx"))
            } else {
                throw Exception("未查到该人员核定数据")
            }
        }
    }

    @CommandLine.Command(
        name = "failList",
        description = ["从业务系统下载支付失败人员名单"]
    )
    class FailedPayList : CommandWithHelp() {
        @CommandLine.Parameters(
            description = ["支付年月, 格式: YYYYMM, 例如: 202101"]
        )
        private var date = ""

        private val outputDir = """D:\待遇核定"""
        private val template = """$outputDir\待遇支付失败人员名单模板.xls"""

        override fun run() {
            val (year, month) = DateTime.split(date)

            val items = Session.use {
                sendService(PaymentQuery(date, state = "0"))
                val result = getResult<PaymentQuery.Item>()

                val items = mutableListOf<PaymentPersonalDetailQuery.Item>()
                result.filter {
                    it.objectType == "1"
                }.forEach { item ->
                    sendService(PaymentPersonalDetailQuery(item))
                    val ppResult = getResult<PaymentPersonalDetailQuery.Item>()
                    items.addAll(ppResult)
                }
                items.filter {
                    !Strings.isNullOrEmpty(it.idCard)
                }.sortedWith { e1, e2 ->
                    Collator.getInstance(Locale.CHINESE).compare(e1.csName, e2.csName)
                }
            }
            if (items.isNotEmpty()) {
                val workbook = Excel.load(template)
                val sheet = workbook.getSheetAt(0)
                val startRow = 1
                var currentRow = startRow

                println("开始导出数据")

                items.withIndex().forEach { (index, item) ->
                    println("${index + 1} ${item.name.fillRight(6)} ${item.idCard} ${item.csName}")
                    sheet.getOrCopyRow(currentRow++, startRow).apply {
                        getCell("A").setValue(item.csName)
                        getCell("B").setValue(item.name)
                        getCell("C").setValue(item.idCard)
                    }
                }

                val path = Paths.get(
                    outputDir,
                    "${year}年${month.stripPrefix("0")}月待遇支付失败人员名单${DateTime.format()}.xls"
                )
                println("保存: $path")

                workbook.save(path)

                println("结束导出数据")
            }
        }
    }
}
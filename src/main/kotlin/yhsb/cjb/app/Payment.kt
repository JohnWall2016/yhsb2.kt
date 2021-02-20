package yhsb.cjb.app

import picocli.CommandLine
import yhsb.base.cmd.CommandWithHelp
import yhsb.base.datetime.DateTime
import yhsb.base.excel.*
import yhsb.base.math.toChinseMoney
import yhsb.base.text.insertBeforeLastSubstring
import yhsb.base.text.stripPrefix
import yhsb.cjb.net.Session
import yhsb.cjb.net.protocol.PaymentPersonalDetailQuery
import yhsb.cjb.net.protocol.PaymentQuery
import yhsb.cjb.net.protocol.getStopInfoByIdCard
import java.math.BigDecimal

@CommandLine.Command(
    description = ["财务支付单生成程序"]
)
class Payment : CommandWithHelp() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(Payment()).execute(*args)
        }
    }

    @CommandLine.Parameters(
        description = ["发放年月: 格式 YYYYMM, 如 201901"]
    )
    private var yearMonth = ""

    @CommandLine.Parameters(
        description = ["业务状态: 0-待支付, 1-已支付, 默认为：所有"]
    )
    private var state = ""

    val template = """D:\支付管理\雨湖区居保个人账户返还表.xlsx"""

    override fun run() {
        val workbook = Excel.load(template)
        val sheet = workbook.getSheetAt(0)

        val (year, month) = DateTime.split(yearMonth)
        sheet.getCell("A1").setCellValue(
            "${year}年${month.stripPrefix("0")}月个人账户返还表"
        )

        val date = DateTime.format()
        val dateCh = DateTime.format("yyyy年M月d日")
        sheet.getCell("H2").setCellValue("制表时间：$dateCh")

        Session.use {
            val startRow = 4
            var currentRow = 4
            var sum: BigDecimal = BigDecimal.ZERO

            sendService(PaymentQuery(yearMonth, state))
            val result = getResult<PaymentQuery.Item>()
            val items = result.sortedWith { e1, e2 ->
                e1.payList - e2.payList
            }

            for (item in items) {
                if (item.objectType == "3") { //个人支付
                    sendService(PaymentPersonalDetailQuery(item))
                    val ppResult = getResult<PaymentPersonalDetailQuery.Item>()
                    val ppItem = ppResult.first()

                    val info = getStopInfoByIdCard(ppItem.idCard, true)

                    sheet.getOrCopyRow(currentRow++, startRow).apply {
                        getCell("A").setValue(currentRow - startRow)
                        getCell("B").setValue(ppItem.name)
                        getCell("C").setValue(ppItem.idCard)
                        getCell("D").setValue(
                            if (info != null) {
                                "${ppItem.type}(${info.reason})"
                            } else {
                                "${ppItem.type}"
                            }
                        )
                        getCell("E").setValue(ppItem.payList)
                        getCell("F").setValue(ppItem.amount)
                        getCell("G").setValue(ppItem.amount.toChinseMoney())
                        getCell("H").setValue(item.name)
                        getCell("I").setValue(item.account)
                        getCell("J").setValue(info?.bankName)

                        sum += ppItem.amount
                    }
                }
            }

            if (currentRow > startRow) {
                sheet.getOrCopyRow(currentRow, startRow).apply {
                    getCell("A").setValue("合计")
                    getCell("F").setValue(sum)
                }

                workbook.save(template.insertBeforeLastSubstring(date))
            }
        }
    }
}
package yhsb.cjb.app

import org.ktorm.dsl.eq
import org.ktorm.entity.find
import picocli.CommandLine
import yhsb.base.cmd.CommandWithHelp
import yhsb.base.datetime.DateTime
import yhsb.base.excel.Excel
import yhsb.base.excel.getCell
import yhsb.base.excel.getOrCopyRow
import yhsb.base.excel.save
import yhsb.base.text.fillRight
import yhsb.cjb.db.JzfpDb2021
import yhsb.cjb.db.historyData
import yhsb.cjb.net.Session
import yhsb.cjb.net.protocol.JbKind
import yhsb.cjb.net.protocol.JoinAuditQuery
import java.nio.file.Paths

@CommandLine.Command(description = ["参保审核与参保身份变更程序"])
class Audit : CommandWithHelp() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(Audit()).execute(*args)
        }
    }

    @CommandLine.Option(names = ["-e", "--export"], description = ["是否导出数据"])
    private var export = false

    @CommandLine.Parameters(paramLabel = "startDate", description = ["开始时间, 例如: 20200701"])
    private var startDate = ""

    @CommandLine.Parameters(paramLabel = "endDate", description = ["结束时间, 例如: 20200701"], index = "1", arity = "0..1")
    private var endDate = ""

    private val outputDir = """D:\特殊缴费\"""

    private val template = "批量信息变更模板.xls"

    data class ChangeInfo(
        val idCard: String,
        val name: String,
        val kind: String,
    )

    override fun run() {
        val startDate = DateTime.toDashedDate(startDate)
        val endDate = if (endDate.isNotEmpty()) DateTime.toDashedDate(endDate) else ""

        val timeSpan = if (endDate.isNotEmpty()) "$startDate<->$endDate" else startDate
        println(timeSpan)

        val result = Session.use {
            sendService(JoinAuditQuery(startDate, endDate))
            getResult<JoinAuditQuery.Item>()
        }

        println("共计 ${result.size()} 条")

        if (result.isNotEmpty()) {
            val changeList = mutableListOf<ChangeInfo>()

            JzfpDb2021.use {
                for (item in result) {
                    val msg = "${item.idCard} ${item.name.fillRight(6)} ${item.birthDay}"

                    val data = historyData.find { it.idCard eq item.idCard }
                    if (data != null) {
                        println("$msg ${data.jbrdsf} ${if (item.name != data.name) data.name else ""}")
                        changeList.add(
                            ChangeInfo(
                                item.idCard,
                                item.name,
                                JbKind.nameMap.getOrDefault(data.jbrdsf, "")
                            )
                        )
                    } else {
                        println(msg)
                    }
                }
            }

            if (export && changeList.isNotEmpty()) {
                val workbook = Excel.load(Paths.get(outputDir, template))
                val sheet = workbook.getSheetAt(0)
                var index = 1
                val copyIndex = 1

                changeList.forEach {
                    sheet.getOrCopyRow(index++, copyIndex, false).apply {
                        getCell("B").setCellValue(it.idCard)
                        getCell("E").setCellValue(it.name)
                        getCell("J").setCellValue(it.kind)
                    }
                }

                workbook.save(Paths.get(outputDir, "批量信息变更(${timeSpan})${DateTime.format()}.xls"))
            }
        }
    }
}
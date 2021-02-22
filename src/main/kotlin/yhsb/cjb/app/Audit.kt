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
import yhsb.base.text.bar
import yhsb.base.text.fillRight
import yhsb.cjb.db.JzfpDb2021
import yhsb.cjb.db.historyData
import yhsb.cjb.net.Session
import yhsb.cjb.net.protocol.*
import java.nio.file.Paths

@CommandLine.Command(
    description = ["城居保数据审核程序"],
    subcommands = [
        Audit.JoinAudit::class,
        Audit.OnlineAudit::class,
    ]
)
class Audit : CommandWithHelp() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(Audit()).execute(*args)
        }
    }

    override fun run() {
        CommandLine.usage(Audit(), System.out)
    }

    @CommandLine.Command(
        name = "join",
        description = ["参保审核与参保身份变更程序"]
    )
    class JoinAudit : CommandWithHelp() {
        @CommandLine.Option(names = ["-e", "--export"], description = ["是否导出数据"])
        private var export = false

        @CommandLine.Parameters(paramLabel = "startDate", description = ["开始时间, 例如: 20200701"])
        private var startDate = ""

        @CommandLine.Parameters(
            paramLabel = "endDate",
            description = ["结束时间, 例如: 20200701"],
            index = "1",
            arity = "0..1"
        )
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
                sendService(JoinAuditQuery(startDate, endDate, auditState = "1"))
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

    @CommandLine.Command(
        name = "online",
        description = ["网上居保审核查询程序"]
    )
    class OnlineAudit : CommandWithHelp() {

        @CommandLine.Parameters(paramLabel = "operator", description = ["网络经办人名称, 默认: wsjb"], defaultValue = "wsjb")
        private var operator = "wsjb"

        override fun run() {
            Session.use {
                println("查询经办人: $operator")

                println(" 参保审核查询 ".bar(60, '='))
                sendService(JoinAuditQuery(operator = operator, auditState = "0"))
                val jaResult = getResult<JoinAuditQuery.Item>()
                jaResult.withIndex().forEach { (i, it) ->
                    println("${(i + 1).toString().fillRight(3)} ${it.name.fillRight(6)} ${it.idCard} ${it.opTime}")
                }

                println(" 缴费人员终止查询 ".bar(60, '='))
                sendService(PayingPersonStopAuditQuery(operator = operator, auditState = "0"))
                val ppsResult = getResult<PayingPersonStopAuditQuery.Item>()
                ppsResult.withIndex().forEach { (i, it) ->
                    println("${(i + 1).toString().fillRight(3)} ${it.name.fillRight(6)} ${it.idCard} ${it.opTime}")
                }

                println(" 待遇人员终止查询 ".bar(60, '='))
                sendService(RetiredPersonStopAuditQuery(operator = operator, auditState = "0"))
                val rpsResult = getResult<RetiredPersonStopAuditQuery.Item>()
                rpsResult.withIndex().forEach { (i, it) ->
                    println("${(i + 1).toString().fillRight(3)} ${it.name.fillRight(6)} ${it.idCard} ${it.opTime}")
                }

                println(" 缴费人员暂停查询 ".bar(60, '='))
                sendService(PayingPersonPauseAuditQuery(auditState = "0"))
                val pppResult = getResult<PayingPersonPauseAuditQuery.Item>()
                pppResult.filter { item ->
                    sendService(PayingPersonPauseAuditDetailQuery(item))
                    getResult<PayingPersonPauseAuditDetailQuery.Item>().any {
                        it.operator == operator
                    }
                }.withIndex().forEach { (i, it) ->
                    println("${(i + 1).toString().fillRight(3)} ${it.name.fillRight(6)} ${it.idCard} ${it.opTime}")
                }

                println(" 待遇人员暂停查询 ".bar(60, '='))
                sendService(RetiredPersonPauseAuditQuery(auditState = "0"))
                val rppResult = getResult<RetiredPersonPauseAuditQuery.Item>()
                rppResult.filter { item ->
                    sendService(RetiredPersonPauseAuditDetailQuery(item))
                    getResult<RetiredPersonPauseAuditDetailQuery.Item>().any {
                        it.operator == operator
                    }
                }.withIndex().forEach { (i, it) ->
                    println("${(i + 1).toString().fillRight(3)} ${it.name.fillRight(6)} ${it.idCard} ${it.opTime}")
                }
            }
        }
    }
}
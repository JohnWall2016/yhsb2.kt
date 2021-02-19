package yhsb.cjb.app

import picocli.CommandLine
import yhsb.base.cmd.CommandWithHelp
import yhsb.base.cmd.ExcelUpdateByPersonCommand
import yhsb.base.excel.*
import yhsb.base.text.fillRight
import yhsb.base.text.insertBeforeLastSubstring
import yhsb.cjb.net.protocol.PersonInfoInProvinceQuery
import yhsb.cjb.net.Session

@CommandLine.Command(
    description = ["城居保信息查询和更新程序"],
    subcommands = [
        Fetch.Query::class,
        Fetch.Update::class
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
        CommandLine.usage(Query(), System.out)
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
                            if (detail) {
                                when (p.cbState.value) {
                                    "2" -> { // 暂停参保
                                        TODO()
                                    }
                                    "4" -> { // 终止参保
                                        TODO()
                                    }
                                }
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

            workbook.save(excel.insertBeforeLastSubstring(".up", "."))

            println("结束数据处理")
        }
    }
}
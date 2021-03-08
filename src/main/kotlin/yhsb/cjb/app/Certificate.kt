package yhsb.cjb.app

import picocli.CommandLine
import yhsb.base.cmd.CommandWithHelp
import yhsb.base.excel.*
import yhsb.cjb.net.protocol.Division.groupByDwAndCsName
import java.nio.file.Files
import java.nio.file.Paths

@CommandLine.Command(
    description = ["资格认证表格生成程序"],
    subcommands = [Certificate.GenerateTable::class]
)
class Certificate : CommandWithHelp() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(Certificate()).execute(*args)
        }
    }

    override fun run() {
        CommandLine.usage(Certificate(), System.out)
    }

    @CommandLine.Command(
        name = "genTable",
        description = ["生成待遇领取人员资格认证表"]
    )
    class GenerateTable : CommandWithHelp() {
        @CommandLine.Parameters(
            paramLabel = "excel",
            description = ["系统导出待认证表格文件路径"]
        )
        private var excel = ""

        @CommandLine.Parameters(
            paramLabel = "startRow",
            description = ["开始行(从1开始)"]
        )
        private var startRow = -1

        @CommandLine.Parameters(
            paramLabel = "endRow",
            description = ["结束行(包含在内)"]
        )
        private var endRow = -1

        @CommandLine.Parameters(
            paramLabel = "outDir",
            description = ["导出认证表格文件路径"]
        )
        private var outputDir = ""

        private val template = """D:\待遇认证\城乡居民基本养老保险待遇领取人员资格认证表（表二）.xls"""

        override fun run() {
            val workbook = Excel.load(excel)
            val sheet = workbook.getSheetAt(0)

            data class Item(val divisionName: String, val idCard: String, val name: String, val sex: String)

            val map = iterator {
                ((startRow - 1) until endRow).forEach {
                    yield(sheet.getRow(it).run {
                        val divisionName = getCell("A").getValue()
                        val name = getCell("C").getValue()
                        val idCard = getCell("D").getValue()
                        val sex = getCell("E").getValue().run {
                            if (this == "1") "男" else "女"
                        }

                        Pair(
                            divisionName, Item(
                                divisionName,
                                idCard,
                                name,
                                sex
                            )
                        )
                    })
                }
            }.groupByDwAndCsName()

            if (!Files.exists(Paths.get(outputDir))) {
                Files.createDirectory(Paths.get(outputDir))
            } else {
                println("目录已存在: $outputDir")
                return
            }

            println("导出目录: $outputDir")

            map.forEach { (dw, subMap) ->
                println("  $dw: ${subMap.values.fold(0) { acc, l -> acc + l.size }}")

                Files.createDirectory(Paths.get(outputDir, dw))

                subMap.forEach { (cs, list) ->
                    println("    $cs: ${list.size}")

                    val outWorkbook = Excel.load(template)
                    val outSheet = outWorkbook.getSheetAt(0)
                    val startRow = 4
                    var currentRow = startRow

                    list.forEach {
                        outSheet.getOrCopyRow(currentRow++, startRow).apply {
                            getCell("A").setValue(currentRow - startRow)
                            getCell("B").setValue(it.name)
                            getCell("C").setValue(it.sex)
                            getCell("D").setValue(it.idCard)
                            getCell("E").setValue(it.divisionName)
                        }
                    }

                    outWorkbook.save(Paths.get(outputDir, dw, "${cs}.xls"))
                }
            }
        }
    }
}
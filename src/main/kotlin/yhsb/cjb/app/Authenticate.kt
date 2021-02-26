package yhsb.cjb.app

import com.google.common.base.Strings
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import picocli.CommandLine
import yhsb.base.cmd.CommandWithHelp
import yhsb.base.excel.Excel
import yhsb.base.excel.getCell
import yhsb.base.excel.getValue
import yhsb.base.text.fillRight
import yhsb.cjb.db.AuthDb2021
import yhsb.cjb.db.RawItem
import yhsb.cjb.db.rawData

@CommandLine.Command(
    description = ["城居参保身份谁程序"],
    subcommands = [
        Authenticate.VeryPoor::class
    ]
)
class Authenticate : CommandWithHelp() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(Authenticate()).execute(*args)
        }

        fun importRawData(items: Iterable<RawItem>) {
            AuthDb2021.use {
                items.withIndex().forEach { (index, item) ->
                    println("${index + 1} ${item.idCard} ${item.name.fillRight(6)} ${item.type}")

                    if (!Strings.isNullOrEmpty(item.idCard)) {
                        val result = rawData.filter {
                            it.idCard eq item.idCard
                        }
                        if (result.isNotEmpty()) {
                            result.forEach {
                                it.update(item)
                                it.flushChanges()
                            }
                        } else {
                            rawData.add(item)
                            item.flushChanges()
                        }
                    }
                }
            }
        }
    }

    override fun run() {
        CommandLine.usage(Authenticate(), System.out)
    }

    abstract class ImportCommand : CommandWithHelp() {
        @CommandLine.Parameters(
            paramLabel = "date",
            description = ["数据月份, 例如: 201912"]
        )
        protected var date = ""

        @CommandLine.Parameters(
            paramLabel = "excel",
            description = ["excel表格文件路径"]
        )
        protected var excel = ""

        @CommandLine.Parameters(
            paramLabel = "startRow",
            description = ["开始行(从1开始)"]
        )
        protected var startRow = 0

        @CommandLine.Parameters(
            paramLabel = "endRow",
            description = ["结束行(包含在内)"]
        )
        protected var endRow = 0

        override fun run() {
            importRawData(fetch())
        }

        class NameIdCardCols(private vararg val cols: Pair<String, String>) : Iterable<Pair<String, String>> {
            override fun iterator() = cols.iterator()
        }

        data class FieldCols(
            val nameIdCards: NameIdCardCols,
            val neighborhood: String,
            val community: String,
            val type: String? = null,
            val transform: RawItem.(type: String) -> Unit
        )

        private fun fetch(): Iterable<RawItem> = Iterable {
            iterator {
                val workbook = Excel.load(excel)
                val sheet = workbook.getSheetAt(0)

                for (index in (startRow - 1) until endRow) {
                    sheet.getRow(index)?.apply {
                        val transform = fieldCols.transform
                        val type = fieldCols.type
                        val neighborhood = getCell(fieldCols.neighborhood).getValue()
                        val community = getCell(fieldCols.community).getValue()


                        fieldCols.nameIdCards.forEach {
                            val name = getCell(it.first).getValue()
                            val idCard = getCell(it.second).getValue().substring(0, 18).toUpperCase()
                            val birthDay = idCard.substring(6, 14)
                            val type = if (type != null) getCell(type).getValue() else ""

                            yield(
                                RawItem {
                                    this.name = name
                                    this.idCard = idCard
                                    this.birthDay = birthDay
                                    this.neighborhood = neighborhood
                                    this.community = community
                                    this.date = date
                                }.apply { transform(type) }
                            )
                        }
                    }
                }
            }
        }

        abstract val fieldCols: FieldCols
    }

    @CommandLine.Command(
        name = "tkry",
        description = ["导入特困人员数据"]
    )
    class VeryPoor : ImportCommand() {
        override val fieldCols
            get() = FieldCols(
                NameIdCardCols(
                    "C" to "D"
                ),
                "A",
                "B"
            ) {
                type = "特困人员"
                detail = "是"
            }
    }
/*
    @CommandLine.Command(
        name = "csdb",
        description = ["导入城市低保数据"]
    )
    class CityAllowance : ImportCommand() {
        override val fieldCols
            get() = FieldCols(
                "特困人员",
                "是",
                NameIdCardCols(
                    "C" to "D"
                ),
                "A",
                "B"
            )
    }
 */
}
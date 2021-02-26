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
        Authenticate.VeryPoor::class,
        Authenticate.CityAllowance::class
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

        data class FieldCols(
            val nameIdCards: List<Pair<String, String>>,
            val neighborhood: String,
            val community: String,
            val address: String? = null,
            val type: String? = null,
            val transform: RawItem.() -> Unit
        )

        private fun fetch(): Iterable<RawItem> = Iterable {
            iterator {
                val workbook = Excel.load(excel)
                val sheet = workbook.getSheetAt(0)

                for (index in (startRow - 1) until endRow) {
                    sheet.getRow(index)?.apply {
                        val transform = fieldCols.transform
                        val type = fieldCols.type?.let {
                            getCell(it).getValue()
                        } ?: ""
                        val address = fieldCols.address?.let {
                            getCell(it).getValue()
                        } ?: ""
                        val neighborhood = getCell(fieldCols.neighborhood).getValue()
                        val community = getCell(fieldCols.community).getValue()

                        fieldCols.nameIdCards.forEach {
                            val name = getCell(it.first).getValue()
                            var idCard = getCell(it.second).getValue()

                            if (!Strings.isNullOrEmpty(name) &&
                                !Strings.isNullOrEmpty(idCard)
                            ) {
                                idCard = idCard.trim()
                                val len = idCard.length

                                if (len < 18) return@forEach
                                if (len > 18) idCard = idCard.substring(0, 18)

                                idCard = idCard.toUpperCase()
                                val birthDay = idCard.substring(6, 14)

                                yield(
                                    RawItem {
                                        this.name = name
                                        this.idCard = idCard
                                        this.birthDay = birthDay
                                        this.neighborhood = neighborhood
                                        this.community = community
                                        this.date = this@ImportCommand.date
                                        this.address = address
                                        this.type = type
                                    }.apply { transform() }
                                )
                            }
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
            get() = FieldCols(listOf("C" to "D"), "A", "B") {
                type = "特困人员"
                detail = "是"
            }
    }

    @CommandLine.Command(
        name = "csdb",
        description = ["导入城市低保数据"]
    )
    class CityAllowance : ImportCommand() {
        override val fieldCols
            get() = FieldCols(
                listOf(
                    "H" to "I",
                    "J" to "K",
                    "L" to "M",
                    "N" to "O",
                    "P" to "Q",
                ),
                "A",
                "B",
                "D",
                "F",
            ) {
                if (type == "全额救助" || type == "全额") {
                    type = "全额低保人员"
                } else {
                    type = "差额低保人员"
                }
            }
    }

}
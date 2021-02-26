package yhsb.cjb.app

import com.google.common.base.Strings
import org.ktorm.dsl.and
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
    description = ["城居参保身份认证程序"],
    subcommands = [
        Authenticate.VeryPoor::class,
        Authenticate.CityAllowance::class,
        Authenticate.CountryAllowance::class,
        Authenticate.Disability::class,
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
                        }.filter {
                            it.type eq item.type
                        }.filter {
                            it.date eq item.date
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
            get() = FieldCols(
                nameIdCards = listOf("C" to "D"),
                neighborhood = "A",
                community = "B"
            ) {
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
                nameIdCards = listOf(
                    "H" to "I",
                    "J" to "K",
                    "L" to "M",
                    "N" to "O",
                    "P" to "Q",
                ),
                neighborhood = "A",
                community = "B",
                address = "D",
                type = "F",
            ) {
                if (type == "全额救助" || type == "全额") {
                    type = "全额低保人员"
                } else {
                    type = "差额低保人员"
                }
                detail = "城市"
            }
    }

    @CommandLine.Command(
        name = "ncdb",
        description = ["导入农村低保数据"]
    )
    class CountryAllowance : ImportCommand() {
        override val fieldCols
            get() = FieldCols(
                nameIdCards = listOf(
                    "G" to "I",
                    "J" to "K",
                    "L" to "M",
                    "N" to "O",
                    "P" to "Q",
                    "R" to "S",
                ),
                neighborhood = "A",
                community = "B",
                address = "D",
                type = "E",
            ) {
                if (type == "全额救助" || type == "全额") {
                    type = "全额低保人员"
                } else {
                    type = "差额低保人员"
                }
                detail = "农村"
            }
    }

    @CommandLine.Command(
        name = "cjry",
        description = ["导入残疾人员数据"]
    )
    class Disability : ImportCommand() {
        override val fieldCols
            get() = FieldCols(
                nameIdCards = listOf(
                    "A" to "B",
                ),
                neighborhood = "F",
                community = "G",
                address = "H",
                type = "K",
            ) {
                detail = type
                when (type) {
                    "一级", "二级" -> type = "一二级残疾人员"
                    "三级", "四级" -> type = "三四级残疾人员"
                    else -> throw Exception("未知残疾类型")
                }
            }
    }

}
package yhsb.cjb.app

import com.google.common.base.Strings
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import picocli.CommandLine
import yhsb.base.cmd.CommandWithHelp
import yhsb.base.db.loadExcel
import yhsb.base.excel.Excel
import yhsb.base.excel.getCell
import yhsb.base.excel.getValue
import yhsb.base.text.fillRight
import yhsb.cjb.db.*

@CommandLine.Command(
    description = ["城居参保身份认证程序"],
    subcommands = [
        Authenticate.VeryPoor::class,
        Authenticate.CityAllowance::class,
        Authenticate.CountryAllowance::class,
        Authenticate.Disability::class,
        Authenticate.MergeHistory::class,
        Authenticate.GenerateBook::class,
        Authenticate.Authenticate::class,
        Authenticate.ImportJbData::class
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
                rawData.run {
                    items.withIndex().forEach { (index, item) ->
                        println("${index + 1} ${item.idCard} ${item.name.fillRight(6)} ${item.type}")

                        if (!Strings.isNullOrEmpty(item.idCard)) {
                            val result = filter {
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
                                result.add(item)
                                item.flushChanges()
                            }
                        }
                    }
                }
            }
        }

        fun mergeRawData(date: String, month: String? = null) {
            AuthDb2021.use {
                val rawDataGroups = rawData.filter {
                    it.date eq date
                }.groupBy {
                    it.idCard
                }

                if (month == null) {
                    historyData.let {
                        rawDataGroups.forEach { (idCard, groups) ->
                            val data = it.filter {
                                it.idCard eq idCard
                            }
                            if (data.isNotEmpty()) {
                                data.forEach { item ->
                                    groups.forEach { rawItem ->
                                        item.merge(rawItem)
                                        item.flushChanges()
                                    }
                                }
                            } else {
                                val item = HistoryItem()
                                groups.forEach { rawItem ->
                                    item.merge(rawItem)
                                }
                                data.add(item)
                                item.flushChanges()
                            }
                        }
                    }
                } else {
                    monthData.let {
                        rawDataGroups.forEach { (idCard, groups) ->
                            val data = it.filter {
                                it.idCard eq idCard
                            }.filter {
                                it.month eq month
                            }
                            if (data.isNotEmpty()) {
                                data.forEach { item ->
                                    groups.forEach { rawItem ->
                                        item.merge(rawItem)
                                        item.flushChanges()
                                    }
                                }
                            } else {
                                val item = MonthItem {
                                    this.month = month
                                }
                                groups.forEach { rawItem ->
                                    item.merge(rawItem)
                                }
                                data.add(item)
                                item.flushChanges()
                            }
                        }
                    }
                }
            }
        }

        fun authenticate(date: String, monthOrAll: String) {
            AuthDb2021.use {
                val data = if (monthOrAll.toUpperCase() == "ALL") {
                    historyData
                } else {
                    monthData
                }

                data.forEach {
                    var jbKind: String? = null
                    var isDestitute: String? = null
                    var i = 1
                    if (!Strings.isNullOrEmpty(it.poverty)) {
                        jbKind = "贫困人口一级"
                        isDestitute = "贫困人口"
                    } else if (!Strings.isNullOrEmpty(it.veryPoor)) {
                        jbKind = "特困一级"
                        isDestitute = "特困人员"
                    } else if (!Strings.isNullOrEmpty(it.fullAllowance)) {
                        jbKind = "低保对象一级"
                        isDestitute = "低保对象"
                    } else if (!Strings.isNullOrEmpty(it.primaryDisability)) {
                        jbKind = "残一级"
                    } else if (!Strings.isNullOrEmpty(it.shortAllowance)) {
                        jbKind = "低保对象二级"
                        isDestitute = "低保对象"
                    } else if (!Strings.isNullOrEmpty(it.secondaryDisability)) {
                        jbKind = "残二级"
                    }
                    var updated = false
                    if (jbKind != null && jbKind != it.jbKind) {
                        if (!Strings.isNullOrEmpty(it.jbKind)) {
                            println("${i++} ${it.idCard} ${it.name.fillRight(6)} $jbKind <- ${it.jbKind}")
                            it.jbKind = jbKind
                            it.jbKindLastDate = date
                        } else {
                            println("${i++} ${it.idCard} ${it.name.fillRight(6)} $jbKind")
                            it.jbKind = jbKind
                            it.jbKindLastDate = date
                        }
                        updated = true
                    }
                    if (isDestitute != null && isDestitute != it.isDestitute) {
                        it.isDestitute = isDestitute
                        updated = true
                    }
                    if (updated) it.flushChanges()
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

    @CommandLine.Command(
        name = "hbdc",
        description = ["合并到扶贫历史数据底册"]
    )
    class MergeHistory : CommandWithHelp() {
        @CommandLine.Parameters(
            paramLabel = "date",
            description = ["数据月份, 例如: 201912"]
        )
        private var date = ""

        override fun run() {
            println("开始合并扶贫数据至: 扶贫历史数据底册")

            mergeRawData(date)

            println("结束合并扶贫数据至: 扶贫历史数据底册")
        }
    }

    @CommandLine.Command(
        name = "scdc",
        description = ["生成当月扶贫数据底册"]
    )
    class GenerateBook : CommandWithHelp() {
        @CommandLine.Parameters(
            paramLabel = "date",
            description = ["数据月份, 例如: 201912"]
        )
        private var date = ""

        @CommandLine.Parameters(
            paramLabel = "clear",
            description = ["是否清除数据表"]
        )
        private var clear = false

        override fun run() {
            if (clear) {
                println("开始清除数据表: ${date}扶贫数据底册")
                AuthDb2021.use {
                    monthData.removeIf {
                        it.month eq date
                    }
                }
                println("结束清除数据表: ${date}扶贫数据底册")
            }

            println("开始合并扶贫数据至: ${date}扶贫数据底册")

            mergeRawData(date, date)

            println("结束合并扶贫数据至: ${date}扶贫数据底册")
        }
    }

    @CommandLine.Command(
        name = "rdsf",
        description = ["认定居保身份"]
    )
    class Authenticate : CommandWithHelp() {
        @CommandLine.Parameters(
            paramLabel = "monthOrAll",
            description = ["数据月份, 例如: 201912, ALL"]
        )
        private var monthOrAll = ""

        @CommandLine.Parameters(
            paramLabel = "date",
            description = ["数据月份，例如：201912"]
        )
        private var date = ""

        override fun run() {
            println("开始认定参保人员身份: ${monthOrAll}扶贫数据底册")

            authenticate(date, monthOrAll)

            println("结束认定参保人员身份: ${monthOrAll}扶贫数据底册")
        }
    }

    @CommandLine.Command(
        name = "drjb",
        description = ["导入居保参保人员明细表"]
    )
    class ImportJbData : CommandWithHelp() {
        @CommandLine.Parameters(
            paramLabel = "excel",
            description = ["excel表格文件路径"]
        )
        private var excel = ""

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

        @CommandLine.Parameters(
            paramLabel = "clear",
            description = ["是否清除数据表"]
        )
        private var clear = false

        override fun run() {
            if (clear) {
                println("开始清除数据表: 居保参保人员明细表")
                AuthDb2021.use {
                    joinedPersonData.clear()
                }
                println("结束清除数据表: 居保参保人员明细表")
            }

            println("开始导入居保参保人员明细表")

            AuthDb2021.use {
                joinedPersonData.loadExcel(excel, startRow, endRow,
                    listOf("E", "A", "B", "C", "F", "G", "I", "K", "L", "O")
                )
            }

            println("结束导入居保参保人员明细表")
        }
    }
}
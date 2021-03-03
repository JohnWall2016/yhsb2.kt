package yhsb.cjb.app

import com.google.common.base.Strings
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.ktorm.dsl.*
import org.ktorm.entity.*
import picocli.CommandLine
import yhsb.base.cmd.CommandWithHelp
import yhsb.base.datetime.DateTime
import yhsb.base.db.execute
import yhsb.base.db.loadExcel
import yhsb.base.excel.*
import yhsb.base.text.fillRight
import yhsb.cjb.db.*
import java.nio.file.Files
import java.nio.file.Paths

@CommandLine.Command(
    description = ["城居参保身份认证程序"],
    subcommands = [
        Authenticate.VeryPoor::class,
        Authenticate.CityAllowance::class,
        Authenticate.CountryAllowance::class,
        Authenticate.Disability::class,
        Authenticate.MergeHistory::class,
        Authenticate.GenerateBook::class,
        Authenticate.AuthData::class,
        Authenticate.ImportJbData::class,
        Authenticate.UpdateJbState::class,
        Authenticate.ExportData::class,
        Authenticate.ExportChangedData::class,
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
                                this.add(item)
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
                }.toList()

                var index = 1
                if (month == null) {
                    historyData.run {
                        rawDataGroups.forEach { (idCard, groups) ->
                            println("${index++} $idCard")

                            val data = filter {
                                it.idCard eq idCard
                            }
                            if (data.isNotEmpty()) {
                                data.forEach { item ->
                                    groups.forEach { rawItem ->
                                        item.merge(rawItem)
                                    }
                                    item.flushChanges()
                                }
                            } else {
                                val item = HistoryItem()
                                groups.forEach { rawItem ->
                                    item.merge(rawItem)
                                }
                                this.add(item)
                                item.flushChanges()
                            }
                        }
                    }
                } else {
                    monthData.let {
                        rawDataGroups.forEach { (idCard, groups) ->
                            println("${index++} $idCard")
                            val data = it.filter {
                                it.idCard eq idCard
                            }.filter {
                                it.month eq month
                            }
                            if (data.isNotEmpty()) {
                                data.forEach { item ->
                                    groups.forEach { rawItem ->
                                        item.merge(rawItem)
                                    }
                                    item.flushChanges()
                                }
                            } else {
                                val item = MonthItem {
                                    this.month = month
                                }
                                groups.forEach { rawItem ->
                                    item.merge(rawItem)
                                }
                                it.add(item)
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
                    monthData.filter { it.month eq monthOrAll }
                }

                var index = 1
                data.forEach {
                    var jbKind: String? = null
                    var isDestitute: String? = null
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
                            println("${index++} ${it.idCard} ${it.name.fillRight(6)} $jbKind <- ${it.jbKind}")
                            it.jbKind = jbKind
                            it.jbKindLastDate = date
                        } else {
                            println("${index++} ${it.idCard} ${it.name.fillRight(6)} $jbKind")
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

        fun exportData(monthOrAll: String, templateExcel: String, saveExcel: String) {
            println("开始导出扶贫底册: ${monthOrAll}扶贫数据=>${saveExcel}")

            val workbook = Excel.load(templateExcel)
            val sheet = workbook.getSheetAt(0)
            val startRow = 2
            var currentRow = startRow

            AuthDb2021.use {
                val data = if (monthOrAll.toUpperCase() == "ALL") {
                    historyData
                } else {
                    monthData.filter {
                        it.month eq monthOrAll
                    }
                }

                data.forEach {
                    val index = currentRow - startRow + 1

                    println("$index ${it.idCard} ${it.name}")

                    sheet.getOrCopyRow(currentRow++, startRow).apply {
                        getCell("A").setValue(index)
                        getCell("B").setValue(it.no)
                        getCell("C").setValue(it.neighborhood)
                        getCell("D").setValue(it.community)
                        getCell("E").setValue(it.address)
                        getCell("F").setValue(it.name)
                        getCell("G").setValue(it.idCard)
                        getCell("H").setValue(it.birthDay)
                        getCell("I").setValue(it.poverty)
                        getCell("J").setValue(it.povertyDate)
                        getCell("K").setValue(it.veryPoor)
                        getCell("L").setValue(it.veryPoorDate)
                        getCell("M").setValue(it.fullAllowance)
                        getCell("N").setValue(it.fullAllowanceDate)
                        getCell("O").setValue(it.shortAllowance)
                        getCell("P").setValue(it.shortAllowanceDate)
                        getCell("Q").setValue(it.primaryDisability)
                        getCell("R").setValue(it.primaryDisabilityDate)
                        getCell("S").setValue(it.secondaryDisability)
                        getCell("T").setValue(it.secondaryDisabilityDate)
                        getCell("U").setValue(it.isDestitute)
                        getCell("V").setValue(it.jbKind)
                        getCell("W").setValue(it.jbKindFirstDate)
                        getCell("X").setValue(it.jbKindLastDate)
                        getCell("Y").setValue(it.jbState)
                        getCell("Z").setValue(it.jbStateDate)
                    }
                }
            }

            workbook.save(saveExcel)

            println("结束导出扶贫底册: ${monthOrAll}扶贫数据=>${saveExcel}")
        }
    }

    override fun run() {
        CommandLine.usage(AuthData(), System.out)
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
                nameIdCards = listOf("G" to "H"),
                neighborhood = "C",
                community = "D",
                address = "E"
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
                    "I" to "J",
                    "K" to "L",
                    "M" to "N",
                    "O" to "P",
                    "Q" to "R",
                ),
                neighborhood = "A",
                community = "B",
                address = "E",
                type = "G",
            ) {
                type = if (type == "全额救助" || type == "全额") {
                    "全额低保人员"
                } else {
                    "差额低保人员"
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
                    "H" to "J",
                    "K" to "L",
                    "M" to "N",
                    "O" to "P",
                    "Q" to "R",
                    "S" to "T",
                ),
                neighborhood = "A",
                community = "B",
                address = "D",
                type = "F",
            ) {
                type = if (type == "全额救助" || type == "全额") {
                    "全额低保人员"
                } else {
                    "差额低保人员"
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
                neighborhood = "E",
                community = "F",
                address = "G",
                type = "O",
            ) {
                detail = type
                type = when (type) {
                    "一级", "二级" -> "一二级残疾人员"
                    "三级", "四级" -> "三四级残疾人员"
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

        @CommandLine.Option(
            names = ["-c", "--clear"],
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
    class AuthData : CommandWithHelp() {
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

        @CommandLine.Option(
            names = ["-c", "--clear"],
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
                joinedPersonData.loadExcel(
                    excel, startRow, endRow,
                    listOf("E", "A", "B", "C", "F", "G", "I", "K", "L", "Q"),
                    printSql = true
                )
            }

            println("结束导入居保参保人员明细表")
        }
    }

    @CommandLine.Command(
        name = "jbzt",
        description = ["更新居保参保状态"]
    )
    class UpdateJbState : CommandWithHelp() {
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

        companion object {
            val jbStateMap = listOf(
                Tuple3(1, 3, "正常待遇"),
                Tuple3(2, 3, "暂停待遇"),
                Tuple3(4, 3, "终止参保"),
                Tuple3(1, 1, "正常缴费"),
                Tuple3(2, 2, "暂停缴费"),
            )
        }

        override fun run() {
            println("开始更新居保状态: ${monthOrAll}扶贫数据底册")

            AuthDb2021.use {
                useConnection {
                    val peopleTable = "jbrymx"
                    if (monthOrAll.toUpperCase() == "ALL") {
                        val dataTable = "fphistorydata"
                        for ((cbState, jfState, jbState) in jbStateMap) {
                            val sql = "update $dataTable $peopleTable\n" +
                                    "    set ${dataTable}.jbcbqk='$jbState',\n" +
                                    "        ${dataTable}.jbcbqkDate='$date'\n" +
                                    " where ${dataTable}.idcard=${peopleTable}.idcard\n" +
                                    "   and ${peopleTable}.cbzt='$cbState'\n" +
                                    "   and ${peopleTable}.jfzt='$jfState'\n"
                            it.execute(sql, true)
                        }
                    } else {
                        val dataTable = "fpmonthdata"
                        for ((cbState, jfState, jbState) in jbStateMap) {
                            val sql = "update $dataTable $peopleTable\n" +
                                    "    set ${dataTable}.jbcbqk='$jbState',\n" +
                                    "        ${dataTable}.jbcbqkDate='$date'\n" +
                                    " where ${dataTable}.month='$monthOrAll'" +
                                    "   and ${dataTable}.idcard=${peopleTable}.idcard\n" +
                                    "   and ${peopleTable}.cbzt='$cbState'\n" +
                                    "   and ${peopleTable}.jfzt='$jfState'\n"
                            it.execute(sql, true)
                        }
                    }
                }
            }

            println("结束更新居保状态: ${monthOrAll}扶贫数据底册")
        }
    }

    @CommandLine.Command(
        name = "dcsj",
        description = ["导出扶贫底册数据"]
    )
    class ExportData : CommandWithHelp() {
        @CommandLine.Parameters(
            paramLabel = "monthOrAll",
            description = ["数据月份, 例如: 201912, ALL"]
        )
        private var monthOrAll = ""

        override fun run() {
            val fileName = if (monthOrAll.toUpperCase() == "ALL") {
                """D:\精准扶贫\2020年度扶贫数据底册${DateTime.format()}.xlsx"""
            } else {
                """D:\精准扶贫\${monthOrAll}扶贫数据底册${DateTime.format()}.xlsx"""
            }

            exportData(monthOrAll, """D:\精准扶贫\雨湖区精准扶贫底册模板.xlsx""", fileName)
        }
    }

    @CommandLine.Command(
        name = "sfbg",
        description = ["导出居保参保身份变更信息表"]
    )
    class ExportChangedData : CommandWithHelp() {
        @CommandLine.Parameters(
            paramLabel = "outputDir",
            description = ["导出目录"]
        )
        private var outputDir = ""

        companion object {
            val jbStateMap = listOf(
                Pair("贫困人口一级", "051"),
                Pair("特困一级", "031"),
                Pair("低保对象一级", "061"),
                Pair("低保对象二级", "062"),
                Pair("残一级", "021"),
                Pair("残二级", "022"),
            )
        }

        override fun run() {
            val template = """D:\精准扶贫\批量信息变更模板.xls"""
            val rowsPerExcel = 500

            if (Files.exists(Paths.get(outputDir))) {
                Files.createDirectory(Paths.get(outputDir))
            } else {
                println("目录已存在: $outputDir")
                return
            }

            data class ChangedData(val name: String?, val idCard: String?, val code: String)

            println("从 扶贫历史数据底册 和 居保参保人员明细表 导出信息变更表")

            AuthDb2021.use {
                for ((type, code) in jbStateMap) {
                    val changedData = from(HistoryData)
                        .innerJoin(JoinedPersonData, on = HistoryData.idCard eq JoinedPersonData.idCard)
                        .select(JoinedPersonData.name, JoinedPersonData.idCard)
                        .where {
                            HistoryData.jbKind eq type
                        }
                        .where {
                            JoinedPersonData.jbKind notEq code
                        }
                        .where {
                            JoinedPersonData.cbState eq "1"
                        }
                        .where {
                            JoinedPersonData.jfState eq "1"
                        }.map {
                            ChangedData(it[JoinedPersonData.name], it[JoinedPersonData.idCard], code)
                        }
                    if (changedData.isNotEmpty()) {
                        println("开始导出 $type 批量信息变更表")

                        var i = 0
                        var files = 0
                        var workbook: Workbook? = null
                        var sheet: Sheet? = null
                        val startRow = 1
                        var currentRow = startRow

                        changedData.forEach {
                            if (i++ % rowsPerExcel == 0) {
                                if (workbook != null) {
                                    workbook?.save(Paths.get(outputDir, "${type}批量信息变更表${++files}.xls"))
                                    workbook = null
                                }
                                if (workbook == null) {
                                    workbook = Excel.load(template)
                                    sheet = workbook?.getSheetAt(0)
                                    currentRow = 1
                                }
                            }
                            sheet?.getOrCopyRow(currentRow++, startRow, false)?.apply {
                                getCell("B").setValue(it.idCard)
                                getCell("E").setValue(it.name)
                                getCell("J").setValue(it.code)
                            }
                            if (workbook != null) {
                                workbook?.save(Paths.get(outputDir, "${type}批量信息变更表${++files}.xls"))
                            }
                        }
                        println("结束导出 $type 批量信息变更表: $i 条")
                    }
                }
            }
        }
    }
}
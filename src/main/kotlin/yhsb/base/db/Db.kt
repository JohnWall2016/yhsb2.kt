package yhsb.base.db

import org.ktorm.database.Database
import org.ktorm.entity.EntitySequence
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.schema.BaseTable
import yhsb.base.excel.Excel
import yhsb.base.excel.getCell
import yhsb.base.excel.getValue
import yhsb.base.text.stripPrefix
import yhsb.base.util.Config
import yhsb.base.util.toMap
import java.nio.file.Files
import java.sql.Connection

open class DbSession(private val configPrefix: String) {
    init {
        Class.forName("yhsb.base.db.MySqlDialect")
    }

    fun getConnection(): Database {
        val cfg = Config.load("$configPrefix.dataSource").toMap()
        return Database.connect(
            cfg["url"].toString(),
            user = cfg["username"].toString(),
            password = cfg["password"].toString(),
            driver = cfg["driverClassName"].toString(),
            logger = ConsoleLogger(threshold = LogLevel.valueOf(cfg["logLevel"].toString()))
        )
    }
/*
    fun <T> use(func: Database.() -> T): T {
        return getConnection().func()
    }
*/
    fun <T> use(func: Database.() -> T): T {
        return getConnection().run {
            useTransaction {
                func()
            }
        }
    }
}

fun Database.execute(sql: String, printSql: Boolean = false, indent: String = ""): Int {
    return useConnection {
        it.execute(sql, printSql, indent)
    }
}

fun Connection.execute(sql: String, printSql: Boolean = false, indent: String = ""): Int {
    if (printSql) println("$indent$sql")
    return createStatement().run {
        execute(sql)
        updateCount
    }
}

/**
 * Load a excel to a database.
 *
 * @param startRow the start row whose index start from 1.
 * @param endRow the end row which is exluded.
 */
fun <E : Any, T : BaseTable<E>> EntitySequence<E, T>.loadExcel(
    fileName: String, startRow: Int, endRow: Int,
    cols: List<String>, noQuotedCols: List<String> = listOf(),
    tableIndex: Int = 0, printSql: Boolean = false
): Int {
    val workbook = Excel.load(fileName)
    val sheet = workbook.getSheetAt(tableIndex)

    val builder = StringBuilder()
    for (index in (startRow - 1) until endRow) {
        val values = mutableListOf<String>()
        sheet.getRow(index).run {
            cols.forEach {
                val value = getCell(it).getValue()
                values.add(
                    if (it in noQuotedCols) {
                        value
                    } else {
                        "'$value'"
                    }
                )
            }
        }
        builder.append(values.joinToString(","))
        builder.append("\n")
    }

    val tmpFile = Files.createTempFile("yhsb-db-", null)
    Files.writeString(tmpFile, builder.toString())

    val sql = "load data infile '${tmpFile.toUri().path.stripPrefix("/")}' into table `${sourceTable}` " +
            "CHARACTER SET utf8 FIELDS TERMINATED BY ',' OPTIONALLY " +
            "ENCLOSED BY '\\'' LINES TERMINATED BY '\\n';"

    val updateCount = this.database.execute(sql, printSql)

    Files.delete(tmpFile)

    return updateCount
}
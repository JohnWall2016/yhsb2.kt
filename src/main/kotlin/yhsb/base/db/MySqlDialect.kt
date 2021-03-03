package yhsb.base.db

import org.ktorm.database.Database
import org.ktorm.database.SqlDialect
import org.ktorm.expression.*
import org.ktorm.schema.Column
import org.ktorm.schema.IntSqlType
import org.ktorm.schema.VarcharSqlType

open class MySqlDialect : SqlDialect {
    override fun createSqlFormatter(database: Database, beautifySql: Boolean, indentSize: Int): SqlFormatter {
        return MySqlFormatter(database, beautifySql, indentSize)
    }
}

object ExtraProperty {
    const val Suffix = "MySqlDialect.suffix"
}

open class MySqlFormatter(
    database: Database, beautifySql: Boolean, indentSize: Int
) : SqlFormatter(database, beautifySql, indentSize) {

    override fun writePagination(expr: QueryExpression) {
        newLine(Indentation.SAME)
        writeKeyword("limit ?, ? ")
        _parameters += ArgumentExpression(expr.offset ?: 0, IntSqlType)
        _parameters += ArgumentExpression(expr.limit ?: Int.MAX_VALUE, IntSqlType)
    }

    override fun <T : Any> visitFunction(expr: FunctionExpression<T>): FunctionExpression<T> {
        writeKeyword("${expr.functionName}(")
        visitExpressionList(expr.arguments)
        removeLastBlank()

        if (ExtraProperty.Suffix in expr.extraProperties) {
            write(" ${expr.extraProperties[ExtraProperty.Suffix]}")
        }

        write(") ")
        return expr
    }
}

fun Column<String>.convert(charset: String): FunctionExpression<String> {
    return FunctionExpression(
        functionName = "convert",
        arguments = listOf(
            this.asExpression()
        ),
        sqlType = VarcharSqlType,
        extraProperties = mapOf(
            ExtraProperty.Suffix to "using $charset"
        )
    )
}

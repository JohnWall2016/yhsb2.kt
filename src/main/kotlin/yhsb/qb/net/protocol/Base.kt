package yhsb.qb.net.protocol

import org.w3c.dom.Document
import org.w3c.dom.Element
import yhsb.base.xml.*

@Namespaces(
    [
        NS("soap", "http://schemas.xmlsoap.org/soap/envelope/")
    ]
)
@Node("soap:Envelope")
class InEnvelope<T : Parameters>(
    params: T
) : ToXml {
    @Attribute("soap:encodingStyle")
    val encodingStyle = "http://schemas.xmlsoap.org/soap/encoding/"

    @Node("soap:Header")
    val header = InHeader(params.funId)

    @Node("soap:Body")
    val body = InBody(params)

    fun setUser(user: String) {
        header.system.user = user
    }

    fun setPassword(password: String) {
        header.system.password = password
    }
}

class InHeader(funId: String) : ToXml {
    @Node("in:system")
    @Namespaces(
        [
            NS("in", "http://www.molss.gov.cn")
        ]
    )
    val system = System(funId)
}

class System(
    funId: String
) : ToXml {
    @AttrNode("para", "usr")
    var user: String = ""

    @AttrNode("para", "pwd")
    var password: String = ""

    @AttrNode("para", "funid")
    val funId: String = funId
}

class InBody<T : ToXml>(
    @Node("in:business")
    @Namespaces(
        [
            NS("in", "http://www.molss.gov.cn/")
        ]
    )
    val business: T
) : ToXml

open class Parameters(
    @Transient val funId: String
) : ToXml


@Namespaces(
    [
        NS("soap", "http://schemas.xmlsoap.org/soap/envelope/")
    ]
)
@Node("soap:Envelope")
class OutEnvelope<T : Parameters>(
    @Attribute("soap:encodingStyle")
    val encodingStyle: String,

    @Node("soap:Header")
    val header: OutHeader,

    @Node("soap:Body")
    val body: OutBody<T>
)

class OutHeader(
    val result: OutResult
)

class OutResult(
    @Attribute("sessionID")
    val sessionId: String,

    @Attribute("message")
    val message: String
)

class OutBody<T>(
    @Node("out:business")
    @Namespaces(
        [
            NS("out", "http://www.molss.gov.cn/")
        ]
    )
    val result: OutBusiness<T>
)

data class OutBusiness<T>(
    @AttrNode("result", "result")
    val result: String,

    @AttrNode("result", "row_count")
    val rowCount: Int?,

    @AttrNode("result", "querysql")
    val querySql: String,

    @Node("resultset", Filter::class)
    val resultSet: ResultSet<T>,

    @Node("resultset", NoFilter::class)
    val otherResultSets: List<ResultSet<T>>
) {
    class Filter : (Element) -> Boolean {
        override fun invoke(p1: Element): Boolean {
            return "^querylist|cxjg".toRegex().matches(p1.getAttribute("name"))
        }
    }

    class NoFilter : (Element) -> Boolean {
        override fun invoke(p1: Element): Boolean {
            return !"^querylist|cxjg".toRegex().matches(p1.getAttribute("name"))
        }
    }
}

class Result(
    @Attribute("result")
    val result: String,

    @Attribute("row_count")
    val rowCount: Int,

    @Attribute("querysql")
    val querySql: String
)

data class ResultSet<T>(
    @Attribute("name")
    val name: String,

    @Node("row")
    val rowList: List<T>
) : Iterable<T> {
    override fun iterator(): Iterator<T> = rowList.iterator()
}

open class FunctionId(
    funId: String,
    functionId: String
) : Parameters(funId) {
    @AttrNode("para", "functionid")
    val functionId = functionId
}

open class Query(
    funId: String,
    functionId: String
) : Parameters(funId) {
    @AttrNode("para", "startrow",)
    val startRow = "1"

    @AttrNode("para", "row_count",)
    val rowCount = "-1"

    @AttrNode("para", "pagesize",)
    val pageSize = "500"

    @AttrNode("para", "functionid",)
    val functionId = functionId
}

open class SimpleClientSql(
    funId: String,
    functionId: String,
    sql: String = ""
) : Parameters(funId) {
    @AttrNode("para", "clientsql")
    val clientSql = sql

    @AttrNode("para", "functionid")
    val functionId = functionId
}

open class ClientSql(
    funId: String,
    functionId: String,
    sql: String = ""
) : Parameters(funId) {
    @AttrNode("para", "startrow",)
    val startRow = "1"

    @AttrNode("para", "row_count",)
    val rowCount = "-1"

    @AttrNode("para", "pagesize",)
    val pageSize = "500"

    @AttrNode("para", "clientsql")
    val clientSql = sql

    @AttrNode("para", "functionid",)
    val functionId = functionId
}

open class AddSql(
    funId: String,
    fid: String,
    addSql: String,
    start: Int = 0,
    pageSize: Int = 0
) : Parameters(funId) {
    @AttrNode("para", "pagesize")
    val pageSize = pageSize

    @AttrNode("para", "addsql")
    val addSql = addSql

    @AttrNode("para", "begin")
    val start = start

    @AttrNode("para", "fid")
    val fid = fid
}

class ParaList(
    val attrs: LinkedHashMap<String, String>,
    val paraList: LinkedHashMap<String, String>
) : ToXml {
    override fun toXmlElement(doc: Document, nodeName: String?, namespaces: Map<String, String>?): Element {
        return doc.createElement(nodeName ?: "paralist", attrs).apply {
            paraList.forEach {
                appendChild(
                    doc.createElement("row", mapOf(it.key to it.value))
                )
            }
        }
    }
}

open class ParamList(
    funId: String,
    attrs: LinkedHashMap<String, String>,
    paraList: LinkedHashMap<String, String>
) : Parameters(funId) {
    @Node("paralist")
    val list = ParaList(attrs, paraList)
}

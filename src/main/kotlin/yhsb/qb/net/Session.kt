package yhsb.qb.net

import org.w3c.dom.Element
import yhsb.base.net.HttpRequest
import yhsb.base.net.HttpSocket
import yhsb.base.reflect.GenericClass
import yhsb.base.util.Config
import yhsb.base.xml.*
import yhsb.qb.net.protocol.Login

class Session(
    host: String,
    port: Int,
    private val userId: String,
    private val password: String,
    private val agencyCode: String,
    private val agencyName: String
) : HttpSocket(host, port, "GBK") {
    private val cookies = mutableMapOf<String, String>()

    private fun createRequest(): HttpRequest = HttpRequest("/sbzhpt/MainServlet", "POST", charset).apply {
        addHeader("SOAPAction", "mainservlet")
        addHeader("Content-Type", "text/html;charset=GBK")
        addHeader("Host", url)
        addHeader("Connection", "keep-alive")
        addHeader("Cache-Control", "no-cache")
        if (cookies.isNotEmpty()) {
            addHeader(
                "Cookie",
                cookies.map { "${it.key}=${it.value}" }.joinToString("; ")
            )
        }
    }

    private fun buildRequest(content: String) = createRequest().apply {
        addBody(content)
    }

    private fun request(content: String) {
        val request = buildRequest(content)
        write(request.toByteArray())
    }

    fun <T : Parameters> sendService(params: T) = request(toService(InEnvelope(params).apply {
        setUser(userId)
        setPassword(password)
    }))

    @Suppress("UNCHECKED_CAST")
    fun <T> getResult(classOfT: Class<T>): OutBusiness<T> {
        val result = readBody()
        val outEnv = fromXml(result, OutEnvelope::class.java, classOfT)
        return outEnv.body.result as OutBusiness<T>
    }

    inline fun <reified T> getResult(): OutBusiness<T> = getResult(T::class.java)

    companion object {
        fun toService(request: ToXml): String {
            return request.toXml()
        }

        fun <T : Any> fromXml(xml: String, classOfT: Class<T>, argClass: Class<*>): T {
            return xml.toXmlElement().toObject(GenericClass(classOfT, argClass))
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> resultFromXml(xml: String, classOfT: Class<T>): OutBusiness<T> {
            return fromXml(xml, OutEnvelope::class.java, classOfT).body.result as OutBusiness<T>
        }

        fun <T> use(
            user: String = "sqb",
            autoLogin: Boolean = true,
            func: Session.() -> T
        ) {
            val user = Config.qbSession.getConfig("users.$user")
            Session(
                Config.qbSession.getString("host"),
                Config.qbSession.getInt("port"),
                user.getString("id"),
                user.getString("pwd"),
                user.getString("agencyCode"),
                user.getString("agencyName")
            ).use {
                if (autoLogin) it.login()
                try {
                    it.func()
                } finally {
                    if (autoLogin) it.logout()
                }
            }
        }
    }

    fun login(): String {
        sendService(Login())

        val header = readHeader()
        if (header.containsKey("set-cookie")) {
            header["set-cookie"]?.forEach {
                val r = Regex("([^=]+?)=(.+?);")
                val m = r.find(it)
                if (m != null) {
                    cookies[m.groupValues[1]] = m.groupValues[2]
                }
            }
        }

        return readBody(header)
    }

    fun logout() {}
}

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

class OutBusiness<T>(
    @AttrNode("result", "result")
    val result: String,

    @AttrNode("result", "row_count")
    val rowCount: Int,

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

class ResultSet<T>(
    @Attribute("name")
    val name: String,

    @Node("row")
    val rowList: List<T>
) : Iterable<T> {
    override fun iterator(): Iterator<T> = rowList.iterator()
}
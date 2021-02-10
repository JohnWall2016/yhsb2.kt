package yhsb.cjb.net

import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import yhsb.base.net.HttpRequest
import yhsb.base.net.HttpSocket
import yhsb.base.util.Config
import yhsb.base.util.json.Json
import yhsb.base.util.json.Jsonable
import yhsb.base.util.structs.ListField
import yhsb.cjb.net.protocol.SysLogin

class Session(
    host: String,
    port: Int,
    private val userId: String,
    private val password: String
) : HttpSocket(host, port) {
    private val cookies = mutableMapOf<String, String>()

    private fun createRequest(): HttpRequest = HttpRequest("/hncjb/reports/crud", "POST").apply {
        addHeader("Host", url)
        addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
        addHeader("Origin", "http://$url")
        addHeader("X-Requested-With", "XMLHttpRequest")
        addHeader(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 5.1) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/39.0.2171.95 Safari/537.36"
        )
        addHeader("Content-Type", "multipart/form-data;charset=UTF-8")
        addHeader("Referer", "http://$url/hncjb/pages/html/index.html")
        addHeader("Accept-Encoding", "gzip, deflate")
        addHeader("Accept-Language", "zh-CN,zh;q=0.8")

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

    fun toService(request: Request): String {
        val service = JsonService(request, userId, password)
        return service.toString()
    }

    fun toService(id: String): String = toService(Request(id))

    fun sendService(request: Request) = request(toService(request))

    fun sendService(id: String) = request(toService(id))

    fun <T : Jsonable> getResult(classOfT: Class<T>): Result<T> {
        val result = readBody()
        return Result.fromJson(result, classOfT)
    }

    inline fun <reified T : Jsonable> getResult(): Result<T> = getResult(T::class.java)

    fun login(): String {
        sendService("loadCurrentUser")

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
        readBody(header)

        sendService(SysLogin(userId, password))
        return readBody()
    }

    fun logout(): String {
        sendService("syslogout")
        return readBody()
    }

    companion object {
        fun <T : Jsonable> fromJson(json: String, classOfT: Class<T>): Result<T> = Result.fromJson(json, classOfT)

        fun <T> use(
            user: String = "002",
            autoLogin: Boolean = true,
            func: Session.() -> T
        ) {
            val user = Config.cjbSession.getConfig("users.$user")
            Session(
                Config.cjbSession.getString("host"),
                Config.cjbSession.getInt("port"),
                user.getString("id"),
                user.getString("pwd")
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
}

open class Request(@Transient val id: String) : Jsonable()

open class PageRequest(
    id: String,
    page: Int = 1,
    pageSize: Int = 15,
    filtering: Map<String, String>? = null,
    sorting: Map<String, String>? = null,
    totals: Map<String, String>? = null
) : Request(id) {
    val page = page

    @SerializedName("pagesize")
    val pageSize = pageSize

    val filtering: List<Map<String, String>> =
        if (filtering != null) listOf(filtering) else listOf()

    val sorting: List<Map<String, String>> =
        if (sorting != null) listOf(sorting) else listOf()

    val totals: List<Map<String, String>> =
        if (totals != null) listOf(totals) else listOf()
}

class JsonService<T : Request>(
    params: T,
    userId: String,
    password: String
) : Jsonable() {
    @SerializedName("serviceid")
    val serviceId = params.id

    val target = ""

    @SerializedName("sessionid")
    val sessionId = null

    @SerializedName("loginname")
    val loginName = userId

    val password = password

    val params = params

    @SerializedName("datas")
    val data = listOf<T>(params)
}

class Result<T : Jsonable> : Jsonable(), Iterable<T> {
    @SerializedName("rowcount")
    var rowCount = 0

    var page = 0

    @SerializedName("pagesize")
    var pageSize = 0

    @SerializedName("serviceid")
    var serviceId = ""

    var type = ""

    var vcode = ""

    var message = ""

    @SerializedName("messagedetail")
    var messageDetail = ""

    @SerializedName("datas")
    var data: ListField<T>? = null

    fun add(d: T) {
        data?.add(d)
    }

    operator fun get(index: Int) = data?.get(index)

    fun size() = data?.size()

    fun isEmpty() = data?.isEmpty() ?: true

    override fun iterator() = (data ?: listOf()).iterator()

    @Transient
    var json: String? = null

    companion object {
        fun <T : Jsonable> fromJson(json: String, classOfT: Class<T>): Result<T> {
            try {
                val typeOf = TypeToken
                    .getParameterized(Result::class.java, classOfT)
                    .type
                val result = Json.fromJson<Result<T>>(json, typeOf)
                result.json = json
                return result
            } catch (ex: Exception) {
                throw Exception("Parse Json Exception: $json", ex)
            }
        }
    }
}

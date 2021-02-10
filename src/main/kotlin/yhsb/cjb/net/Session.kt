package yhsb.cjb.net

import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import yhsb.base.net.HttpRequest
import yhsb.base.net.HttpSocket
import yhsb.base.util.json.Json
import yhsb.base.util.json.Jsonable
import yhsb.base.util.structs.ListField

class Session(
    host: String,
    port: Int,
    private val userId: String,
    private val password: String
) : HttpSocket(host, port) {
    val cookies = mutableMapOf<String, String>()

    fun createRequest(): HttpRequest = HttpRequest("/hncjb/reports/crud", "POST").apply {
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

    fun buildRequest(content: String) = createRequest().apply {
        addBody(content)
    }

    fun request(content: String) {
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

    companion object {
        fun <T : Jsonable> fromJson(json: String, classOfT: Class<T>): Result<T> = Result.fromJson(json, classOfT)
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

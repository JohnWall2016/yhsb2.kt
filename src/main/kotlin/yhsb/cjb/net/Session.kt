package yhsb.cjb.net

import yhsb.base.net.HttpRequest
import yhsb.base.net.HttpSocket
import yhsb.base.util.Config
import yhsb.base.json.Jsonable
import yhsb.cjb.net.protocol.JsonService
import yhsb.cjb.net.protocol.Request
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
        return service.toJson()
    }

    fun toService(id: String): String = toService(Request(id))

    fun sendService(request: Request) = request(toService(request))

    fun sendService(id: String) = request(toService(id))

    fun <T : Any> getResult(classOfT: Class<T>): yhsb.cjb.net.protocol.Result<T> {
        val result = readBody()
        return yhsb.cjb.net.protocol.Result.fromJson(result, classOfT)
    }

    inline fun <reified T : Any> getResult(): yhsb.cjb.net.protocol.Result<T> = getResult(T::class.java)

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
        fun <T : Jsonable> fromJson(json: String, classOfT: Class<T>): yhsb.cjb.net.protocol.Result<T> =
            yhsb.cjb.net.protocol.Result.fromJson(json, classOfT)

        fun <T> use(
            user: String = "002",
            autoLogin: Boolean = true,
            func: Session.() -> T
        ): T {
            val user = Config.cjbSession.getConfig("users.$user")
            return Session(
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
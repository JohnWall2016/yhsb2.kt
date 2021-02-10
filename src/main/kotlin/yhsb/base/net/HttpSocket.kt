package yhsb.base.net

import yhsb.base.text.*
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.OutputStream
import java.net.Socket

open class HttpSocket(
    val host: String,
    val port: Int,
    val charset: String = "UTF-8",
) : Closeable {
    val url = "$host:$port"

    private val socket = Socket(host, port)

    private val input = socket.getInputStream()

    private val output = socket.getOutputStream()

    override fun close() {
        output.close()
        input.close()
        socket.close()
    }

    fun write(bytes: ByteArray) = output.write(bytes)

    fun write(s: String) = write(s.toByteArray(charset))

    fun readLine(): String = ByteArrayOutputStream(512).use {
        end@ while (true) {
            when (val c = input.read()) {
                -1 -> break@end
                0x0D -> {// \r
                    when (val n = input.read()) {
                        -1 -> {
                            it.write(c)
                            break@end
                        }
                        0x0A -> // \n
                            break@end
                        else -> {
                            it.write(c)
                            it.write(n)
                        }
                    }
                }
                else -> it.write(c)
            }
        }
        it.toString(charset)
    }

    fun readHeader(): HttpHeader = HttpHeader().apply {
        while (true) {
            val line = readLine()
            // println(line)
            if (line.isEmpty()) break
            val i = line.indexOf(':')
            if (i >= 0) {
                addValue(line.substring(0, i).trim(), line.substring(i + 1).trim())
            }
        }
    }

    fun transfer(to: OutputStream, len: Int) = to.write(input.readNBytes(len))

    fun readBody(header: HttpHeader? = null) = ByteArrayOutputStream(512).use { out ->
        val header = header ?: readHeader()
        // println(header)
        if (header["Transfer-Encoding"]?.any { it == "chunked" } == true) {
            while (true) {
                val len = readLine().toInt(16)
                if (len <= 0) {
                    readLine()
                    break
                } else {
                    transfer(out, len)
                    readLine()
                }
            }
        } else if (header.containsKey("Content-Length")) {
            val len = header["Content-Length"]?.get(0)?.toInt(10) ?: -1
            if (len > 0) {
                transfer(out, len)
            }
        } else {
            throw Exception("unsupported transfer mode")
        }
        out.toString(charset)
    }

    fun getHttp(path: String): String {
        val request = HttpRequest(path, "GET").apply {
            addHeader("Host", url)
            addHeader("Connection", "keep-alive")
            addHeader("Cache-Control", "max-age=0")
            addHeader("Upgrade-Insecure-Requests", "1")
            addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) "
                        + "AppleWebKit/537.36 (KHTML, like Gecko) "
                        + "Chrome/71.0.3578.98 " + "Safari/537.36"
            )
            addHeader(
                "Accept",
                "text/html,applicationxhtml+xml,application/xml;"
                        + "q=0.9,image/webpimage/apng,*/*;q=0.8"
            )
            addHeader("Accept-Encoding", "gzip,deflate")
            addHeader("Accept-Language", "zh-CN,zh;q=09")
        }
        write(request.toByteArray())
        return readBody()
    }
}
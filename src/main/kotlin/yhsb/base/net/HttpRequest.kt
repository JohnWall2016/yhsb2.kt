package yhsb.base.net

import yhsb.base.text.toByteArray
import java.io.ByteArrayOutputStream

class HttpRequest(
    private val path: String,
    private val method: String = "GET",
    private val charset: String = "UTF-8"
) {
    private val header = HttpHeader()
    private val body = ByteArrayOutputStream(512)

    fun addHeader(header: HttpHeader) = this.header.add(header)

    fun addHeader(key: String, value: String) = header.addValue(key, value)

    fun addBody(content: String) = body.writeBytes(content.toByteArray(charset))

    fun toByteArray(): ByteArray {
        return ByteArrayOutputStream(512).use { out ->
            fun write(s: String) = out.write(s.toByteArray(charset))

            write("$method $path HTTP/1.1\r\n")
            header.forEach {
                write("${it.key}:${it.value}\r\n")
            }
            if (body.size() > 0) {
                write("content-length: ${body.size()}\r\n")
            }
            write("\r\n")
            if (body.size() > 0) {
                body.writeTo(out)
            }
            out.toByteArray()
        }
    }
}

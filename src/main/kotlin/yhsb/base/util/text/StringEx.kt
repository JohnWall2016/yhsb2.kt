package yhsb.base.util.text

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
fun String.toByteArray(charsetName: String): ByteArray = (this as java.lang.String).getBytes(charsetName)

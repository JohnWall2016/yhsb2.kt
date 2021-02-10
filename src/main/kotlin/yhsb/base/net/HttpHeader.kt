package yhsb.base.net

import yhsb.base.util.collections.MapEntry

class HttpHeader : Iterable<Map.Entry<String, String>> {
    private val header = mutableMapOf<String, MutableList<String>>()

    fun addValue(key: String, value: String) {
        val k = key.toLowerCase()
        if (!header.containsKey(k)) {
            header[k] = mutableListOf()
        }
        header[k]?.add(value)
    }

    fun add(other: HttpHeader) = this.header.putAll(other.header)

    fun remove(key: String) = header.remove(key.toLowerCase())

    fun clear() = header.clear()

    fun containsKey(key: String): Boolean = header.containsKey(key.toLowerCase())

    operator fun get(key: String): List<String>? = header[key.toLowerCase()]

    override fun iterator(): Iterator<Map.Entry<String, String>> {
        return header.entries.flatMap { e ->
            e.value.map { v -> MapEntry(e.key, v) }
        }.iterator()
    }

    override fun toString(): String {
        return this.joinToString("\n")
    }
}
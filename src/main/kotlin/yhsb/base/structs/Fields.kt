package yhsb.base.structs

import yhsb.base.json.Jsonable

abstract class MapField {
    abstract fun getValueMap(): Map<String, String>

    var value: String = ""

    val name: String get() = getValueMap().getOrDefault(value, "未知值: $value")

    override fun toString() = name
}

class ListField<T : Jsonable> : Iterable<T> {
    val items = mutableListOf<T>()

    override fun iterator(): Iterator<T> = items.iterator()

    fun add(e: T) = items.add(e)

    operator fun get(index: Int) = items[index]

    fun size() = items.size

    fun isEmpty() = items.isEmpty()
}
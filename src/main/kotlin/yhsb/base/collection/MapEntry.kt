package yhsb.base.collection

class MapEntry<K, V>(override val key: K, override val value: V): Map.Entry<K, V> {
    override fun toString(): String {
        return "$key: $value"
    }
}
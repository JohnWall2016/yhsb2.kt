package yhsb.base.collection

import kotlin.NoSuchElementException

open class LinkedNode<T>(
    val data: T,
    next: LinkedNode<T>? = null
) : Iterable<T> {
    override fun iterator(): Iterator<T> {
        return LinkedNodeIter(this)
    }

    private var _next = next

    var next: LinkedNode<T>?
        get() = _next
        internal set(value) {
            _next = value
        }

    fun isEmpty() = iterator().hasNext() == false

    class LinkedNodeIter<T>(
        private var cursor: LinkedNode<T>?
    ) : Iterator<T> {
        override fun hasNext(): Boolean {
            return cursor != null
        }

        override fun next(): T {
            val data = cursor?.data ?: throw NoSuchElementException()
            cursor = cursor?.next
            return data
        }
    }

    private object Empty : LinkedNode<Nothing?>(null) {
        override fun iterator(): Iterator<Nothing?> {
            return LinkedNodeIter(null)
        }
    }

    companion object {
        fun <T> of(list: List<T>): LinkedNode<T> {
            val size = list.size
            return when {
                size >= 2 -> LinkedNode(list.first(), of(list.drop(1)))
                size == 1 -> LinkedNode(list.first())
                else -> empty()
            }
        }

        fun <T> of(vararg e: T): LinkedNode<T> = of(e.toList())

        @Suppress("UNCHECKED_CAST")
        fun <T> empty(): LinkedNode<T> {
            return Empty as LinkedNode<T>
        }
    }

    override fun toString(): String {
        return "[${joinToString()}}]"
    }
}

fun <T> Collection<LinkedNode<T>>.flattenToLinkedNode(): LinkedNode<T> {
    if (isEmpty()) return LinkedNode.empty()

    val iter = iterator()
    val first = iter.next()
    var cur: LinkedNode<T>? = first
    while (iter.hasNext()) {
        while (cur?.next != null) {
            cur = cur.next
        }
        val next = iter.next()
        cur?.next = next
        cur = next
    }
    return first
}
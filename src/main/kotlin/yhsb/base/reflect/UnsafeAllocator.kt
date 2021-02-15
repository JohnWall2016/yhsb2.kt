package yhsb.base.reflect

import java.lang.reflect.Method

@Suppress("UNCHECKED_CAST")
object UnsafeAllocator {
    private var unsafe: Any? = null
    private var allocateInstance: Method? = null
    private var exception: Exception? = null

    init {
        try {
            val unsafeClass = Class.forName("sun.misc.Unsafe")
            val f = unsafeClass.getDeclaredField("theUnsafe")
            f.setAccessible(true)
            unsafe = f.get(null)
            allocateInstance = unsafeClass.getMethod("allocateInstance", Class::class.java)
        } catch (e: Exception) {
            exception = e
        }
    }

    fun <T> newInstance(c: Class<T>): T = allocateInstance?.invoke(unsafe, c) as T ?: throw exception!!
}
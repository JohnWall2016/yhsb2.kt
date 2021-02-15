package yhsb.base.reflect

import java.lang.Exception
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

class GenericClass<T>(
    val rawClass: Class<T>,
    val typeArguments: List<Class<*>> = listOf()
) {

    val typeParametersMap = LinkedHashMap<TypeVariable<*>, Class<*>>()

    init {
        if (rawClass.typeParameters.size != typeArguments.size)
            throw IllegalArgumentException(
                "the size is mismatched, $rawClass expect ${rawClass.typeParameters.size} " +
                        "got ${typeArguments.size} $typeArguments"
            )
        rawClass.typeParameters.forEachIndexed { index, typeVar ->
            typeParametersMap[typeVar] = typeArguments[index]
        }
    }

    constructor(rawClass: Class<T>, vararg typeArguments: Class<*>)
            : this(rawClass, typeArguments.toList())

    fun resolveTypeParameter(param: TypeVariable<*>): Class<*>? = typeParametersMap[param]

    override fun toString(): String {
        return "GenericClass<$rawClass<${typeParametersMap.values.map { it.toString() }.joinToString()}>>"
    }

    fun newInstance(): T {
        try {
            return rawClass.getConstructor().newInstance()
        } catch (ex: Exception) {
            try {
                return UnsafeAllocator.newInstance(rawClass)
            } catch (ex2: Exception) {
                throw Exception("Cannot create instance of ${rawClass}: \n${ex}\n${ex2}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> createGenericClass(type: Type): GenericClass<T> {
        //println("$type: ${type.javaClass}")
        return if (type is Class<*>) {
            GenericClass(type) as GenericClass<T>
        } else if (type is ParameterizedType) {
            val rawClass = type.rawType as Class<*>

            val actualClassArguments =  type.actualTypeArguments.map {
                var arg: Class<*>? = null
                if (it is TypeVariable<*>) {
                    arg = resolveTypeParameter(it)
                } else if (it is Class<*>) {
                    arg = it
                }
                if (arg == null) throw Exception("Cannot resolve type argument: $it")
                else arg
            }
            GenericClass(rawClass, actualClassArguments) as GenericClass<T>
        } else if (type is TypeVariable<*>) {
            GenericClass(resolveTypeParameter(type) as Class<T>)
        } else {
            throw Exception("Cannot create GenericClass: $type")
        }
    }
}

inline fun <reified T : Annotation> Field.getAnnotation(): T? = getAnnotation(T::class.java)

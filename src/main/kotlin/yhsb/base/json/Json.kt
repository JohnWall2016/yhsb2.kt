package yhsb.base.json

import com.google.gson.*
import yhsb.base.struct.ListField
import yhsb.base.struct.MapField
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

interface JsonAdapter<T> : JsonSerializer<T>, JsonDeserializer<T>

class JsonFieldAdapter : JsonAdapter<MapField> {
    override fun serialize(src: MapField, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.value)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MapField {
        val classOfT = typeOfT as Class<*>
        val field = classOfT.getConstructor().newInstance() as MapField
        field.value = json.asString ?: ""
        return field
    }
}

class ListFieldAdapter : JsonAdapter<ListField<Any>> {
    override fun serialize(src: ListField<Any>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return context.serialize(src.items)
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ListField<Any> {
        val paramType = typeOfT as ParameterizedType
        val rawType = paramType.rawType as Class<*>
        val argType = paramType.actualTypeArguments[0] as Class<*>

        @Suppress("UNCHECKED_CAST")
        val field = rawType.getConstructor().newInstance() as ListField<Any>

        if (json is JsonArray) {
            json.forEach { e ->
                if (e is JsonObject && e.size() > 0) {
                    field.items.add(Json.fromJson(e, argType))
                }
            }
        }
        return field
    }
}

object Json {
    private val gson =
        GsonBuilder()
            .serializeNulls()
            .registerTypeHierarchyAdapter(MapField::class.java, JsonFieldAdapter())
            .registerTypeHierarchyAdapter(ListField::class.java, ListFieldAdapter())
            .create()

    fun <T> toJson(obj: T) = gson.toJson(obj)

    fun <T> fromJson(json: String, typeOfT: Type): T = gson.fromJson(json, typeOfT)

    inline fun <reified T> fromJson(json: String): T = fromJson(json, T::class.java)

    fun <T> fromJson(element: JsonElement, typeOfT: Type): T = gson.fromJson(element, typeOfT)

    inline fun <reified T> fromJson(element: JsonElement): T = fromJson(element, T::class.java)
}

abstract class Jsonable {
    fun toJson(): String = Json.toJson(this)

    override fun toString() = toJson()
}
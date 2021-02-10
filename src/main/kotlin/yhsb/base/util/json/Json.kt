package yhsb.base.util.json

import com.google.gson.*
import yhsb.base.util.structs.ListField
import yhsb.base.util.structs.MapField
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

interface JsonAdapter<T> : JsonSerializer<T>, JsonDeserializer<T>

class JsonFieldAdapter : JsonAdapter<MapField> {
    override fun serialize(src: MapField?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.value)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): MapField {
        val classOfT = typeOfT as Class<*>
        val field = classOfT.getConstructor().newInstance() as MapField
        field.value = json?.asString ?: ""
        return field
    }
}

class DataFieldAdapter : JsonAdapter<ListField<*>> {
    override fun serialize(src: ListField<*>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        return context?.serialize(src, typeOfSrc)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ListField<*> {
        val paramType = typeOfT as ParameterizedType
        val rawType = paramType.rawType as Class<*>
        val argType = paramType.actualTypeArguments[0] as Class<*>

        val field = rawType.getConstructor().newInstance() as ListField<*>

        if (json is JsonArray) {
            json.forEach { e ->
                if (e is JsonObject && e.size() > 0) {
                    @Suppress("UNREACHABLE_CODE", "IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
                    field.items.add(Json.fromJson(e, argType))
                }
            }
        }
        return field
    }
}

object Json {
    private val gson =
        GsonBuilder().apply {
            serializeNulls()
            registerTypeHierarchyAdapter(MapField::class.java, JsonFieldAdapter())
            registerTypeHierarchyAdapter(ListField::class.java, DataFieldAdapter())
        }.create()

    fun <T> toJson(obj: T) = gson.toJson(obj)

    fun <T> fromJson(json: String, typeOfT: Type): T = gson.fromJson(json, typeOfT)

    fun <T> fromJson(element: JsonElement, typeOfT: Type): T = gson.fromJson(element, typeOfT)
}

abstract class Jsonable {
    fun toJson(): String = Json.toJson(this)

    override fun toString() = toJson()
}
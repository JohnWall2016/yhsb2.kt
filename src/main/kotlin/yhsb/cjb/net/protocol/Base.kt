package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import yhsb.base.json.Json
import yhsb.base.json.Jsonable
import yhsb.base.struct.ListField


open class Request(@Transient val id: String) : Jsonable()

open class PageRequest(
    id: String,
    page: Int = 1,
    pageSize: Int = 15,
    filtering: Map<String, String>? = null,
    sorting: Map<String, String>? = null,
    totals: Map<String, String>? = null
) : Request(id) {
    val page = page

    @SerializedName("pagesize")
    val pageSize = pageSize

    val filtering: List<Map<String, String>> =
        if (filtering != null) listOf(filtering) else listOf()

    val sorting: List<Map<String, String>> =
        if (sorting != null) listOf(sorting) else listOf()

    val totals: List<Map<String, String>> =
        if (totals != null) listOf(totals) else listOf()
}

class JsonService<T : Request>(
    params: T,
    userId: String,
    password: String
) : Jsonable() {
    @SerializedName("serviceid")
    val serviceId = params.id

    val target = ""

    @SerializedName("sessionid")
    val sessionId = null

    @SerializedName("loginname")
    val loginName = userId

    val password = password

    val params = params

    @SerializedName("datas")
    val data = listOf<T>(params)
}

class Result<T : Jsonable> : Jsonable(), Iterable<T> {
    @SerializedName("rowcount")
    var rowCount = 0

    var page = 0

    @SerializedName("pagesize")
    var pageSize = 0

    @SerializedName("serviceid")
    var serviceId = ""

    var type = ""

    var vcode = ""

    var message = ""

    @SerializedName("messagedetail")
    var messageDetail = ""

    @SerializedName("datas")
    var data: ListField<T>? = null

    fun add(d: T) {
        data?.add(d)
    }

    operator fun get(index: Int) = data?.get(index)

    fun size() = data?.size()

    fun isEmpty() = data?.isEmpty() ?: true

    override fun iterator() = (data ?: listOf()).iterator()

    @Transient
    var json: String? = null

    companion object {
        fun <T : Jsonable> fromJson(json: String, classOfT: Class<T>): Result<T> {
            try {
                val typeOf = TypeToken
                    .getParameterized(Result::class.java, classOfT)
                    .type
                val result = Json.fromJson<Result<T>>(json, typeOf)
                result.json = json
                return result
            } catch (ex: Exception) {
                throw Exception("Parse Json Exception: $json", ex)
            }
        }
    }
}

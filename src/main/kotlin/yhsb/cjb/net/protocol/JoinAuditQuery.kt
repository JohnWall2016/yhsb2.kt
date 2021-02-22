package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/** 参保审核查询 */
class JoinAuditQuery(
    startDate: String = "",
    endDate: String = "",
    auditState: String = "0",
    operator: String = ""
) : PageRequest(
    "cbshQuery",
    pageSize = 500
) {
    val aaf013 = ""
    val aaf030 = ""

    @SerializedName("aae016")
    val auditState = auditState

    @SerializedName("aae011")
    val operator = operator

    val aae036 = ""
    val aae036s = ""

    val aae014 = ""

    @SerializedName("aae015")
    val startDate = startDate

    @SerializedName("aae015s")
    val endDate = endDate

    val aac009 = ""
    val aac002 = ""
    val aac003 = ""
    val sfccb = ""

    data class Item(
        @SerializedName("aac002")
        val idCard: String,

        @SerializedName("aac003")
        val name: String,
    
        @SerializedName("aac006")
        val birthDay: String,

        @SerializedName("aaf102")
        override val czName: String?,

        @SerializedName("aae011")
        val operator: String,

        /** 经办时间 */
        @SerializedName("aae036")
        val opTime: String
    ) : XzqhName
}
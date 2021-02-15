package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/** 参保审核查询 */
class JoinAuditQuery(
    startDate: String,
    endDate: String,
    auditState: String = "1"
) : PageRequest(
    "cbshQuery",
    pageSize = 500
) {
    val aaf013 = ""
    val aaf030 = ""
    val aae011 = ""
    val aae036 = ""
    val aae036s = ""
    val aae014 = ""
    val aac009 = ""
    val aac002 = ""
    val aac003 = ""
    val sfccb = ""

    @SerializedName("aae015")
    val startDate = startDate

    @SerializedName("aae015s")
    val endDate = endDate

    @SerializedName("aae016")
    val auditState = auditState

    data class Item(
        @SerializedName("aac002")
        val idCard: String,

        @SerializedName("aac003")
        val name: String,
    
        @SerializedName("aac006")
        val birthDay: String
    )
}
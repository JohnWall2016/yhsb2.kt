package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/** 待遇人员终止审核查询 */
class RetiredPersonStopAuditQuery(
    idCard: String
) : PageRequest("dyzzfhPerInfoList") {
    val aaf013 = ""
    val aaf030 = ""
    val aae016 = ""
    val aae011 = ""
    val aae036 = ""
    val aae036s = ""
    val aae014 = ""
    val aae015 = ""
    val aae015s = ""

    @SerializedName("aac002")
    val idCard = idCard

    val aac003 = ""
    val aac009 = ""
    val aae160 = ""

    val aic301 = ""

    data class Item(
        @SerializedName("aac002")
        val idCard: String,

        @SerializedName("aac003")
        val name: String,

        /** 终止年月 */
        @SerializedName("aae031")
        val stopYearMonth: String,

        /** 审核日期 */
        @SerializedName("aae015")
        val auditDate: String,
    
        val aaz176: Int,
    )
}
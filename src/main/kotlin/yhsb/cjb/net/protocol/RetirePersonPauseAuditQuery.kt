package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/** 待遇人员暂停审核查询 */
class RetirePersonPauseAuditQuery(
    idCard: String
) : PageRequest("queryAllPausePersonInfosForAuditService") {
    val aaf013 = ""
    val aaz070 = ""

    @SerializedName("aac002")
    val idCard = idCard

    /** 起始暂停年月 */
    val aae141 = ""
    /** 截止暂停年月 */
    val aae141s = ""

    /** 审核状态 */
    val aae016 = ""

    /** 起始经办时间 */
    val aae036 = ""
    /** 截止经办时间 */
    val aae036s = ""

    /** 户籍性质 */
    val aac009 = ""

    /** 起始审核时间 */
    val aae015 = ""
    /** 截止审核时间 */
    val aae015s = ""

    /** 待遇状态 */
    val aae116 = ""

    data class Item(
        @SerializedName("aac002")
        val idCard: String,

        @SerializedName("aac003")
        val name: String,

        /** 暂停年月 */
        @SerializedName("aae141")
        val pauseYearMonth: Int,

        /** 暂停原因? */
        @SerializedName("aae160")
        val reason: PauseReason,

        /** 审核日期 */
        @SerializedName("aae015")
        val auditDate: String,

        /** 村组名称 */
        @SerializedName("aaf102")
        override val czName: String,

        @SerializedName("aae013")
        val memo: String
    ) : XzqhName
}
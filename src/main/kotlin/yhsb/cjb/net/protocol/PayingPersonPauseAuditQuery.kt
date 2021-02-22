package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/** 缴费人员暂停审核查询 */
class PayingPersonPauseAuditQuery(
    idCard: String = "",
    auditState: String = "0"
) : PageRequest(
    "queryJfZtPersonInfosForAuditService",
    pageSize = 500
) {
    val aaf013 = ""
    val aaz070 = ""

    @SerializedName("aac002")
    val idCard = idCard

    /** 起始经办时间 */
    val aae036 = ""
    /** 截止经办时间 */
    val aae036s = ""

    /** 审核状态 */
    @SerializedName("aae016")
    val auditState = auditState

    /** 起始审核时间 */
    val aae015 = ""
    /** 截止审核时间 */
    val aae015s = ""

    /** 户籍性质 */
    val aac009 = ""

    data class Item(
        @SerializedName("aac002")
        val idCard: String,

        @SerializedName("aac003")
        val name: String,

        /** 暂停年月 */
        @SerializedName("aae035")
        val pauseYearMonth: String,

        /** 暂停原因? */
        @SerializedName("aae160")
        val reason: PauseReason,

        /** 经办时间 */
        @SerializedName("aae036")
        val opTime: String,

        /** 审核日期 */
        @SerializedName("aae015")
        val auditDate: String?,

        @SerializedName("aae016")
        val auditState: AuditState,

        /** 村组名称 */
        @SerializedName("aaf102")
        override val czName: String,

        @SerializedName("aae013")
        val memo: String,

        @SerializedName("aaz163")
        val id: Int
    ) : XzqhName
}
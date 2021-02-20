package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/** 缴费人员终止审核查询 */
class PayingPersonStopAuditQuery(
    idCard: String
) : PageRequest("cbzzfhPerInfoList") {
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

    data class Item(
        @SerializedName("aac002")
        val idCard: String,

        @SerializedName("aac003")
        val name: String,

        /** 终止年月 */
        @SerializedName("aae031")
        val stopYearMonth: Int,

        @SerializedName("aae160")
        val reason: StopReason,

        /** 审核日期 */
        @SerializedName("aae015")
        val auditDate: String?,

        @SerializedName("aae016")
        val auditState: AuditState,
        /** 备注 */
        @SerializedName("aae013")
        val memo: String,

        /** 银行户名 */
        @SerializedName("aae009")
        val bankName: String,

        /** 银行账号 */
        @SerializedName("aae010")
        val bankAccount: String,

        /** 退款金额 */
        @SerializedName("aae025")
        val refundAmount: BigDecimal,

        val aaz038: Int,
        val aac001: Int
    )
}
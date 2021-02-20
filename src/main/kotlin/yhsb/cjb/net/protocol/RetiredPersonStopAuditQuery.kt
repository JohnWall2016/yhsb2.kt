package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

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
        @SerializedName("aic301")
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

        /** 多拨扣回额 */
        @SerializedName("aae422")
        val chargeBackAmount: BigDecimal,

        /** 抵扣金额 */
        @SerializedName("aae424")
        val deductAmount: BigDecimal,

        /** 退款金额 */
        @SerializedName("aae425")
        val refundAmount: BigDecimal,
    
        val aaz176: Int,
    )
}
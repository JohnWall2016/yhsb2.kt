package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/**
 * 代发支付单明细查询
 */
class DelegatePaymentDetailQuery(
    payList: Int,
    page: Int = 1,
    pageSize: Int = 500
) : PageRequest(
    "dfpayffzfdjmxQuery", page, pageSize
) {
    /** 支付单号 */
    @SerializedName("aaz031")
    val payList = "$payList"

    data class Item(
        /** 个人编号 */
        @SerializedName("aac001")
        val pid: Int,

        /** 身份证号码 */
        @SerializedName("aac002")
        val idCard: String,
    
        @SerializedName("aac003")
        val name: String,

        /** 村社区名称 */
        @SerializedName("aaf103")
        val csName: String?,

        /** 支付标志 */
        @SerializedName("aae117")
        val flag: String,

        /** 发放年月 */
        @SerializedName("aae002")
        val yearMonth: Int,

        /** 付款单号 */
        @SerializedName("aaz031")
        val payList: Int,

        /** 个人单号 */
        @SerializedName("aaz220")
        val personalPayList: Long,

        /** 支付总金额 */
        @SerializedName("aae019")
        val amount: BigDecimal
    )
}
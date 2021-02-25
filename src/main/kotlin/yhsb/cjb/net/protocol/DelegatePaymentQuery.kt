package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/**
 * 代发支付单查询
 */
class DelegatePaymentQuery(
    type: String,
    yearMonth: String,
    state: String = "0",
) : PageRequest(
    "dfpayffzfdjQuery"
) {
    /** 代发类型 */
    @SerializedName("aaa121")
    val type = type

    /** 支付单号 */
    @SerializedName("aaz031")
    val payList = ""

    /** 发放年月 */
    @SerializedName("aae002")
    val yearMonth = yearMonth

    @SerializedName("aae089")
    val state = state

    data class Item(
        /** 业务类型中文名 */
        @SerializedName("aaa121")
        val typeCh: String?,

        /** 付款单号 */
        @SerializedName("aaz031")
        val payList: Int,

        /** 支付银行编码 */
        @SerializedName("bie013")
        val bankType: String?
    )
}
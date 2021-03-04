package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/** 财务支付管理查询 */
class PaymentQuery(
    yearMonth: String,
    state: String = "0"
) : PageRequest(
    "cwzfglQuery",
    1, 100,
    totals = mapOf(
        "dataKey" to "aae169",
        "aggregate" to "sum"
    )
) {
    /** 支付类型 */
    @SerializedName("aaa121")
    val type = ""

    /** 支付单号 */
    @SerializedName("aaz031")
    val payList = ""

    /** 发放年月 */
    @SerializedName("aae002")
    val yearMonth = yearMonth

    @SerializedName("aae089")
    val state = state

    val bie013 = ""

    data class Item(
        /** 支付对象类型: "1" - 月度银行代发, "3" - 个人支付*/
        @SerializedName("aaa079")
        val objectType: String,

        /** 支付单号 */
        @SerializedName("aaz031")
        val payList: Int,

        /** 支付状态 */
        @SerializedName("aae088")
        val state: String,

        @SerializedName("aaa121")
        val type: PayType,

        /** 发放年月 */
        @SerializedName("aae002")
        val yearMonth: Int,

        /** 支付对象银行户名 */
        @SerializedName("aae009")
        val name: String,

        /** 支付银行编码 */
        @SerializedName("bie013")
        val bankType: String,

        /** 支付对象银行账号 */
        @SerializedName("aae010")
        val account: String
    )
}
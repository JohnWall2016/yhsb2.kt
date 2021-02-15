package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/** 财务支付管理查询_支付单人员明细 */
class PaymentPersonalDetailQuery(
    payList: String = "",
    yearMonth: String = "",
    state: String = "",
    type: String = ""
) : PageRequest(
    "cwzfgl_zfdryQuery",
    1, 1000,
    totals = mapOf(
        "dataKey" to "aae019",
        "aggregate" to "sum"
    )
) {
    val aaf015 = ""

    @SerializedName("aac002")
    val idCard = ""

    @SerializedName("aac003")
    val name = ""

    /**
     * 支付单号
     */
    @SerializedName("aaz031")
    val payList = payList

    /**
     * 支付状态
     */
    @SerializedName("aae088")
    val state = state

    /**
     * 业务类型: "F10004" - 重复缴费退费; "F10007" - 缴费调整退款;
     * "F10006" - 享受终止退保
     */
    @SerializedName("aaa121")
    val type = type

    /**
     * 发放年月
     */
    @SerializedName("aae002")
    val yearMonth = yearMonth

    data class Item(
        /** 身份证号码 */
        @SerializedName("aac002")
        val idCard: String,

        @SerializedName("aac003")
        val name: String,

        /** 付款单号 */
        @SerializedName("aaz031")
        val payList: Int,

        /** 支付总金额 */
        @SerializedName("aae019")
        val amount: BigDecimal,
    
        @SerializedName("aaa121")
        val type: PayType
    )
}
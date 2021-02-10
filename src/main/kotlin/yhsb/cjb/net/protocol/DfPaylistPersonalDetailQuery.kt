package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName
import yhsb.cjb.net.PageRequest
import java.math.BigDecimal

/**
 * 代发支付单个人明细查询
 */
class DfPaylistPersonalDetailQuery(
    pid: Int,
    payList: Int,
    personalPayList: Long,
    page: Int = 1,
    pageSize: Int = 500
) : PageRequest(
    "dfpayffzfdjgrmxQuery", page, pageSize
) {
    /** 个人编号 */
    @SerializedName("aac001")
    val pid = "$pid"

    /** 支付单号 */
    @SerializedName("aaz031")
    val payList = "$payList"

    /** 支付单号 */
    @SerializedName("aaz220")
    val personalPayList = "$personalPayList"

    data class Item(
        /** 待遇日期 */
        @SerializedName("aae003")
        val date: Int,

        /** 支付标志 */
        @SerializedName("aae117")
        val flag: String,

        /** 发放年月 */
        @SerializedName("aae002")
        val yearMonth: String,

        /** 付款单号 */
        @SerializedName("aaz031")
        val payList: Int,

        /** 支付总金额 */
        @SerializedName("aae019")
        val amount: BigDecimal
    )
}
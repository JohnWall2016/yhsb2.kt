package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/**
 * 代发人员名单查询
 */
class DfPersonQuery(
    type: String,
    cbState: String,
    dfState: String,
    page: Int = 1,
    pageSize: Int = 100,
) : PageRequest(
    "executeDfrymdQuery",
    page,
    pageSize,
    sorting = mapOf(
        "dataKey" to "aaf103",
        "sortDirection" to "ascending"
    )
) {
    val aaf013 = ""
    val aaf030 = ""

    @SerializedName("aae100")
    val cbState = cbState

    val aac002 = ""
    val aac003 = ""

    @SerializedName("aae116")
    val state = dfState

    val aac082 = ""

    @SerializedName("aac066")
    val type = type

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
        val csName: String,

        /** 代发开始年月 */
        @SerializedName("aic160")
        val startYearMonth: Int,

        /** 代发标准 */
        @SerializedName("aae019")
        val standard: BigDecimal,

        /** 代发类型 */
        @SerializedName("aac066s")
        val type: String,

        /** 代发状态 */
        @SerializedName("aae116")
        val dfState: DfState,

        /** 居保状态 */
        @SerializedName("aac008s")
        val cbState: CbState,

        /** 代发截至成功发放年月 */
        @SerializedName("aae002jz")
        val endYearMonth: Int,

        /** 代发截至成功发放金额*/
        @SerializedName("aae019jz")
        val totalPayed: BigDecimal,
    )
}
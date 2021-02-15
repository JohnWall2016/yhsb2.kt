package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/** 待遇人员终止审核个人信息查询 */
class RetirePersonStopAuditDetailQuery(
    item: RetiredPersonStopAuditQuery.Item
) : Request("dyzzfhPerinfo") {
    val aaz176 = "${item.aaz176}"

    data class Item(
        @SerializedName("aae160")
        val reason: StopReason,

        @SerializedName("aaz065")
        val bankType: BankType
    )
}
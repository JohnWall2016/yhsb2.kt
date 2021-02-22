package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/** 待遇人员暂停审核查询 查看 */
class RetiredPersonPauseAuditDetailQuery(
    item: RetiredPersonPauseAuditQuery.Item
) : Request("viewPayPauseInfoService") {
    val aac002 = item.idCard
    val aaz173 = "${item.aaz173}"

    data class Item(
        @SerializedName("aae160")
        val reason: PauseReason,

        @SerializedName("aae011")
        val operator: String,

        /** 经办时间 */
        @SerializedName("aae036")
        val opTime: String
    )
}
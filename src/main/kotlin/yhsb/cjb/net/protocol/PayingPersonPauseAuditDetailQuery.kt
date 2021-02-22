package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/** 缴费人员暂停审核查询 查看 */
class PayingPersonPauseAuditDetailQuery(
    item: PayingPersonPauseAuditQuery.Item
) : Request("viewPauseInfoService") {
    @SerializedName("id")
    val opId = "${item.id}"

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
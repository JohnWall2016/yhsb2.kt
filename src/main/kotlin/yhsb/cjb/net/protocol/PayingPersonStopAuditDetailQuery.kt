package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/** 缴费人员终止审核个人信息查询 */
class PayingPersonStopAuditDetailQuery(
    item: PayingPersonStopAuditQuery.Item
) : Request("cbzzfhPerinfo") {
    val aaz038 = "${item.aaz038}"
    val aac001 = "${item.aac001}"
    val aae160 = item.reason.value

    data class Item(
        @SerializedName("aae160")
        val reason: StopReason,

        @SerializedName("aaz065")
        val bankType: BankType
    )
}
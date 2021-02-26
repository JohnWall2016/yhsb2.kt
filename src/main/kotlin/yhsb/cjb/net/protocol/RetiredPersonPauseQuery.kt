package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/** 待遇人员暂停查询 */
class RetiredPersonPauseQuery(
    idCard: String
) : PageRequest("queryAllPausePersonInfosService") {
    val aaf013 = ""
    val aaf030 = ""

    @SerializedName("aac002")
    val idCard = idCard

    val aae141 = ""
    val aae141s = ""

    val aac009 = ""

    val aae036 = ""
    val aae036s = ""

    data class Item(
        @SerializedName("aac002")
        val idCard: String,

        @SerializedName("aac003")
        val name: String,

        /** 暂停年月? */
        @SerializedName("aae002")
        val pauseYearMonth: Int,

        /** 暂停原因? */
        @SerializedName("aae160")
        val reason: PauseReason,

        /** 村组名称 */
        @SerializedName("aaf102")
        override val czName: String,

        @SerializedName("aae013")
        val memo: String
    ) : DivisionName
}
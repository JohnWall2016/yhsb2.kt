package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName
import yhsb.base.json.Jsonable
import yhsb.cjb.net.Request

/**
 * 省内参保信息查询
 */
class PersonInProvinceQuery(
    @SerializedName("aac002") val idCard: String
) : Request("executeSncbxxConQ") {

    data class Item(
        /** 个人编号  */
        @SerializedName("aac001")
        val pid: Int,

        /** 身份证号码 */
        @SerializedName("aac002")
        val idCard: String?,

        @SerializedName("aac003")
        val name: String,

        @SerializedName("aac006")
        val birthDay: String,

        @SerializedName("aac008")
        override val cbState: CbState,

        @SerializedName("aac031")
        override val jfState: JfState,

        /** 参保时间 */
        @SerializedName("aac049")
        val cbTime: Int,

        /** 参保身份编码 */
        @SerializedName("aac066")
        val jbKind: JbKind,

        /** 社保机构 */
        @SerializedName("aaa129")
        val agency: String,

        /** 经办时间 */
        @SerializedName("aae036")
        val opTime: String,

        /** 村组名称 */
        @SerializedName("aaf102")
        override val czName: String,

    ) : JbState, XzqhName {

        fun invalid(): Boolean = idCard.isNullOrEmpty()

        fun valid(): Boolean = !invalid()
    }
}
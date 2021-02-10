package yhsb.cjb.net.protocol


import com.google.gson.annotations.SerializedName
import yhsb.base.util.json.Jsonable
import yhsb.cjb.net.Request

/**
 * 省内参保信息查询
 */
class SncbxxQuery(
    @SerializedName("aac002") val idCard: String
) : Request("executeSncbxxConQ") {

    class Item : Jsonable(), JbState, XzqhName {
        /** 个人编号  */
        @SerializedName("aac001")
        var pid = 0

        /** 身份证号码 */
        @SerializedName("aac002")
        var idCard: String? = null

        @SerializedName("aac003")
        var name: String? = null

        @SerializedName("aac006")
        var birthDay: String? = null

        @SerializedName("aac008")
        override var cbState: CbState? = null

        @SerializedName("aac031")
        override var jfState: JfState? = null

        /** 参保时间 */
        @SerializedName("aac049")
        var cbTime = 0

        /** 参保身份编码 */
        @SerializedName("aac066")
        var jbKind: JbKind? = null

        /** 社保机构 */
        @SerializedName("aaa129")
        var agency: String? = null

        /** 经办时间 */
        @SerializedName("aae036")
        var opTime: String? = null

        /** 村组名称 */
        @SerializedName("aaf102")
        override var czName: String? = null

        fun invalid(): Boolean = idCard.isNullOrEmpty()

        fun valid(): Boolean = !invalid()
    }
}
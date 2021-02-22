package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

/** 个人信息综合查询 */
class PersonInfoQuery(
    idCard: String,
    name: String,
) : PageRequest("zhcxgrinfoQuery") {
    val aaf013 = ""
    val aaz070 = ""
    val aaf101 = ""
    val aac009 = ""

    /** 参保状态: "1"-正常参保 "2"-暂停参保 "4"-终止参保 "0"-未参保 */
    @SerializedName("aac008")
    val cbState = ""

    /** 缴费状态: "1"-参保缴费 "2"-暂停缴费 "3"-终止缴费 */
    @SerializedName("aac031")
    val jfState = ""

    val aac006str = ""
    val aac006end = ""

    val aac066 = ""

    val aae030str = ""
    val aae030end = ""

    val aae476 = ""
    val aae480 = ""
    val aae479 = ""
    val aac058 = ""

    @SerializedName("aac002")
    val idCard = idCard

    val aae478 = ""

    @SerializedName("aac003")
    val name = name

    data class Item(
        @SerializedName("aac001")
        val pid: Int,

        @SerializedName("aac002")
        val idCard: String,
    
        @SerializedName("aac003")
        val name: String,
    
        @SerializedName("aac006")
        val birthDay: String,
    
        @SerializedName("aac008")
        override val cbState: CbState,

        /** 户口所在地 */
        @SerializedName("aac010")
        val hkArea: String,
    
        @SerializedName("aac031")
        override val jfState: JfState,
    
        @SerializedName("aae005")
        val phoneNumber: String,
    
        @SerializedName("aae006")
        val address: String,
    
        @SerializedName("aae010")
        val bankCardNumber: String,

        /** 乡镇街区划编码 */
        @SerializedName("aaf101")
        val xzName: String,

        /** 村社名称区划编码 */
        @SerializedName("aaf102")
        val csName: String,

        /** 组队名称区划编码 */
        @SerializedName("aaf103")
        val zdName: String,
    ) : JbState
}
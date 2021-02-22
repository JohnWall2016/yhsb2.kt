package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/** 省内参保信息查询 - 缴费信息 */
class PayingInfoQuery(
    idCard: String
) : PageRequest(
    "executeSncbqkcxjfxxQ", 1, 500
) {
    @SerializedName("aac002")
    val idCard = idCard

    data class Item(
        /** 缴费年度  */
        @SerializedName("aae003")
        val year: Int?,

        /** 备注  */
        @SerializedName("aae013")
        val memo: String,

        /** 金额  */
        @SerializedName("aae022")
        val amount: BigDecimal,

        @SerializedName("aaa115")
        val type: JfType,
    
        @SerializedName("aae341")
        val item: JfItem,
    
        @SerializedName("aab033")
        val method: JfMethod,
    
        /** 划拨日期  */
        @SerializedName("aae006")
        val paidOffDay: String?,
    
        /** 社保机构  */
        @SerializedName("aaa027")
        val agency: String,
    
        /** 行政区划代码  */
        @SerializedName("aaf101")
        val xzqh: String
    ) {
        /** 是否已划拨 */
        fun isPaidOff() = paidOffDay != null
    }
}
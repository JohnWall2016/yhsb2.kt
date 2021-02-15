package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

class BankInfoQuery(
    @SerializedName("aac002")
    val idCard: String
) : Request("executeSncbgrBankinfoConQ") {
    data class Item(
        /** 银行类型 */
        @SerializedName("bie013")
        val bankType: BankType,

        /** 户名 */
        @SerializedName("aae009")
        val countName: String,

        /** 卡号 */
        @SerializedName("aae010")
        val cardNumber: String
    )
}
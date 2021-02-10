package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName
import yhsb.cjb.net.Request

class SysLogin(
    @SerializedName("username") val userName: String,
    @SerializedName("passwd") val password: String
) : Request("syslogin")

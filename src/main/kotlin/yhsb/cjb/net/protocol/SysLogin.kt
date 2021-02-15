package yhsb.cjb.net.protocol

import com.google.gson.annotations.SerializedName

class SysLogin(
    @SerializedName("username") val userName: String,
    @SerializedName("passwd") val password: String
) : Request("syslogin")

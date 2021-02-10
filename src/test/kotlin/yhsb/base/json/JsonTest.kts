package yhsb.base.json

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Person(
    @SerializedName("aac002") val idCard: String,
    val name: String,
    val salary: BigDecimal,
) : Jsonable()

val p = Json.fromJson<Person>("""{"aac002":"12345","name2":"Peter","salary":3.23}""")

println(p)
println(p.name)
package yhsb.cjb.db

import org.ktorm.dsl.eq
import org.ktorm.entity.*

AuthDb2021.use {

    val result = historyData.filter {
        it.idCard eq "430302200101155040"
    }

    if (result.isNotEmpty()) {
        println(result.first())
    }

}

val a = HistoryItem()
println(a)

val b = HistoryItem {
    idCard = "123"
    name = "abc"
}
println(b)
println(b.no)

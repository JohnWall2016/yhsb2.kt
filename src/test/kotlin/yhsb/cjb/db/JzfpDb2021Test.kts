package yhsb.cjb.db

import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.first
import org.ktorm.entity.isNotEmpty

AuthDb2021.use {

    val result = historyData.filter {
        it.idCard eq "430302200101155040"
    }

    if (result.isNotEmpty()) {
        println(result.first())
    }
}


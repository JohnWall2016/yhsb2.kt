package yhsb.cjb.net

import yhsb.cjb.net.protocol.PaymentQuery
import yhsb.cjb.net.protocol.PersonInfoInProvinceQuery

Session.use {
    sendService(PersonInfoInProvinceQuery("430311194511291027"))
    val result = getResult<PersonInfoInProvinceQuery.Item>()
    println(result)
    result.forEach {
        println(it)
        println("${it.cbState} ${it.jfState} ${it.jbKind} ${it.jbState}")
        println(it.czName)
        println(it.dwName)
    }

    sendService(PaymentQuery("202101", "1"))
    val result2 = getResult<PaymentQuery.Item>()
    println(result2)
    result2.forEach {
        println(it)
    }
}
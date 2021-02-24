package yhsb.cjb.net

import yhsb.cjb.net.protocol.*

Session.use {
    /*
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
    */

    /*sendService(PayingPersonPauseAuditQuery("430321196810130559"))
    println(readBody())*/
    /*println(getPauseInfoByIdCard("430321196810130559"))
    println(getPauseInfoByIdCard("430302191912225020"))
    println(getStopInfoByIdCard("430302192603161021"))
    println(getStopInfoByIdCard("430321196602051581"))*/
    println(getStopInfoByIdCard("430321195209020514", true))
}
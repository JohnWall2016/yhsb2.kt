package yhsb.qb.net

import yhsb.qb.net.protocol.AgencyCodeQuery
import yhsb.qb.net.protocol.JoinedPersonInProvinceQuery
import yhsb.qb.net.protocol.RetiredPersonQuery

Session.use("qqb") {

    sendService(AgencyCodeQuery())
    val acResult = getResult<AgencyCodeQuery.Item>()

    fun getAgencyCode(name: String) = acResult.resultSet.find {
        it.name == name
    }?.code ?: throw Exception("Cannot find agency code")

    sendService(JoinedPersonInProvinceQuery("430302195806251012"))
    val jpResult = getResult<JoinedPersonInProvinceQuery.Item>()

    jpResult.resultSet.forEach {
        println(it)

        sendService(RetiredPersonQuery(it.idCard, getAgencyCode(it.agencyName)))
        val rpResult = getResult<RetiredPersonQuery.Item>()
        rpResult.resultSet.forEach(::println)
    }
}
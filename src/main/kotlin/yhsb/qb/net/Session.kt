package yhsb.qb.net

import yhsb.base.xml.*


@Namespaces([
    NS("soap", "http://schemas.xmlsoap.org/soap/envelope/")
])
@Node("soap:Envelope")
class InEnvelope<T : Parameters>(
    params: T
) : ToXml {
    @Attribute("soap:encodingStyle")
    val encodingStyle = "http://schemas.xmlsoap.org/soap/encoding/"

    @Node("soap:Header")
    val header = InHeader(params.funId)

    @Node("soap:Body")
    val body = InBody(params)

    fun setUser(user: String) {
        header.system.user = user
    }

    fun setPassword(password: String) {
        header.system.password = password
    }
}

class InHeader(funId: String) : ToXml {
    @Node("in:system")
    @Namespaces([
        NS("in", "http://www.molss.gov.cn")
    ])
    val system = System(funId)
}

class System(
    funId: String
) : ToXml {
    @AttrNode("para", "usr")
    var user: String = ""

    @AttrNode("para", "pwd")
    var password: String = ""

    @AttrNode("para", "funid")
    val funId: String = funId
}

class InBody<T : ToXml>(
    @Node("in:business")
    @Namespaces([
        NS("in", "http://www.molss.gov.cn/")
    ])
    val business: T
) : ToXml

open class Parameters(
    @Transient val funId: String
) : ToXml
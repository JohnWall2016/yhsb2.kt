package yhsb.base.xml

import yhsb.base.reflect.GenericClass

/*
import yhsb.base.text.bar
import yhsb.qb.net.InEnvelope
import yhsb.qb.net.Parameters

println("start".bar(60, '='))
println(InEnvelope(Parameters("abc")).toXml())
println("end".bar(60, '='))
*/

val xml = """<?xml version="1.0" encoding="GBK"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" soap:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
  <soap:Header>
    <in:system xmlns:in="http://www.molss.gov.cn/">
      <para usr="abc"/>
      <para pwd="YLZ_A2ASSDFDFDSS"/>
      <para funid="F00.01.03"/>
    </in:system>
  </soap:Header>
  <soap:Body>
    <in:business xmlns:in="http://www.molss.gov.cn/">
      <para startrow="1"/>
      <para row_count="-1"/>
      <para pagesize="500"/>
      <para clientsql="( aac002 = &apos;430302195806251012&apos;)"/>
      <para functionid="F27.06"/>
      <paraset name="paralist">
        <row aac003="徐A" rown="1" />
        <row aac003="徐B" rown="2" />
        <row aac003="徐C" rown="3" />
        <row aac003="徐D" rown="4" />
      </paraset>
    </in:business>
  </soap:Body>
 </soap:Envelope>"""

@Namespaces([
    NS("soap", "http://schemas.xmlsoap.org/soap/envelope/")
])
@Node("soap:Envelope")
class InEnvelope<T : Parameters>(
    @Attribute("soap:encodingStyle")
    val encodingStyle: String,

    @Node("soap:Header")
    val header: InHeader,

    @Node("soap:Body")
    val body: InBody<T>,
) : ToXml

class InHeader(
    @Node("in:system")
    @Namespaces([
        NS("in", "http://www.molss.gov.cn")
    ])
    val system: System
) : ToXml

class System(
    @AttrNode("para", "usr")
    val user: String,

    @AttrNode("para", "pwd")
    val password: String,

    @AttrNode("para", "funid")
    val funId: String,
) : ToXml

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

class Business(
    @AttrNode("para", "startrow")
    val startRow: String,

    @AttrNode("para", "row_count")
    val rowCount: String,

    @AttrNode("para", "pagesize")
    val pageSize: String,

    @AttrNode("para", "clientsql")
    val clientSql: String,

    @AttrNode("para", "functionid")
    val functionId: String,

    @Node("paraset")
    val paraSet: ParaSet
) : ToXml

class ParaSet(
    @Attribute("name")
    val name: String,

    @Node("row")
    val rowList: List<Row>
) : ToXml

class Row(
    @Attribute("aac003")
    val name: String,

    @Attribute("rown")
    val number: String
) : ToXml

val obj = xml.toXmlElement().toObject(GenericClass(InEnvelope::class.java, Business::class.java))

println(obj.toXml(true))
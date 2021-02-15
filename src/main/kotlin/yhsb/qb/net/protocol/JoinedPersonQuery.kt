package yhsb.qb.net.protocol

import yhsb.base.xml.AttrNode
import yhsb.base.xml.Attribute
import yhsb.base.xml.ToXml

/** 参保人员查询统计 */
class JoinedPersonQuery(
    idCard: String,
    agencyCode: String
) : ClientSql(
    "F00.01.03",
    "F27.02",
    "( AC01.AAC002 = &apos;$idCard&apos;"
) {
    @AttrNode("para", "aab034")
    val agencyCode = agencyCode

    class Item(
        @Attribute("aab004")
        val companyName: String,

        @Attribute("sab100")
        val companyCode: String,

        @Attribute("aac001")
        val id: String,

        @Attribute("aac002")
        val idCard: String,

        @Attribute("aac003")
        val name: String,

        @Attribute("sac100")
        val pid: String,

        @Attribute("aab034")
        val agencyCode: String,
    ) : ToXml
}
package yhsb.qb.net.protocol

import yhsb.base.xml.AttrNode
import yhsb.base.xml.Attribute
import yhsb.base.xml.ToXml

/** 离退休人员参保查询统计 */
class RetiredPersonQuery(
    idCard: String,
    agencyCode: String
) : ClientSql(
    "F00.01.03",
    "F27.03",
    "( v.aac002 = &apos;${idCard}&apos;)"
) {
    @AttrNode("para", "aab034")
    val agencyCode = agencyCode

    data class Item(
        @Attribute("aab004")
        val companyName: String,

        /** 待遇发放姿态 */
        @Attribute("aae116")
        val payState: String,

        /** 离退休日期 */
        @Attribute("aic162")
        val retireDate: String,

        /** 待遇开始时间 */
        @Attribute("aic160")
        val startTime: String,

        /** 退休金 */
        @Attribute("txj")
        val pension: String
    ) : ToXml
}
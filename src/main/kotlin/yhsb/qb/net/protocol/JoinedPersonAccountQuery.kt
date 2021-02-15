package yhsb.qb.net.protocol

import yhsb.base.xml.AttrNode
import yhsb.base.xml.Attribute
import yhsb.base.xml.ToXml

/** 参保人员查询统计 - 账户信息 */
class JoinedPersonAccountQuery(
    id: String,
    agencyCode: String
) : AddSql(
    "F27.00.01",
    "F27.02.06",
    "aac001 = &apos;${id}&apos;",
    0,
    0
) {
    @AttrNode("para", "aab034")
    val agencyCode = agencyCode

    class Item(
        /** 开始日期 */
        @Attribute("ksny")
        val startMonth: Int,

        /** 截止日期 */
        @Attribute("jzny")
        val endMonth: Int,

        /** 当年缴费月数 */
        @Attribute("dnjfys")
        val payMonths: Int,

        /** 累计缴费月数 */
        @Attribute("ljjfys")
        val totalPayMonths: Int
    ) : ToXml
}
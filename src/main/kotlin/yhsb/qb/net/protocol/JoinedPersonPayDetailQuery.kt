package yhsb.qb.net.protocol

import yhsb.base.xml.AttrNode
import yhsb.base.xml.Attribute
import yhsb.base.xml.ToXml

/** 参保人员查询统计 - 缴费记录 */
class JoinedPersonPayDetailQuery(
    id: String,
    agencyCode: String
) : AddSql(
    "F27.00.01",
    "F27.02.04",
    "aac001 = &apos;${id}&apos; and aae140 = &apos;1&apos;",
    1,
    500
) {
    @AttrNode("para", "aab034")
    val agencyCode = agencyCode

    class Item(
        @Attribute("aac003")
        val name: String,

        /** 费款所属期 */
        @Attribute("aae002")
        val period: String,

        /** 对应费款所属期 */
        @Attribute("aae003")
        val corPeriod: String,

        /** 缴费月数 */
        @Attribute("sac047")
        val payMonths: Int, // 0 or 1

        @Attribute("sab100")
        val companyCode: String,

        @Attribute("aab004")
        val companyName: String,

        /** 缴费基数 */
        @Attribute("jfjs00")
        val payoffSalary: String,
    ) : ToXml
}
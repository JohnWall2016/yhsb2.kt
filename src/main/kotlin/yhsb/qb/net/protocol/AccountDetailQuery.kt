package yhsb.qb.net.protocol

import yhsb.base.xml.Attribute
import yhsb.base.xml.ToXml

/** 养老个人账户明细查询 */
class AccountDetailQuery(
    id: String
) : ClientSql(
    "F00.01.02",
    "F03.01.19.01",
    "a.aac001 = &apos;${id}&apos;"
) {
    class Item(
        @Attribute("aae001")
        val year: String,

        /** 社平公资 */
        @Attribute("saa014")
        val averageSalary: String,

        /** 缴费基数 */
        @Attribute("aic020")
        val payoffSalary: String,

        /** 缴费指数 */
        @Attribute("aic110")
        val index: String,

        /** 实缴月数 */
        @Attribute("aic090")
        val months: String
    ) : ToXml
}
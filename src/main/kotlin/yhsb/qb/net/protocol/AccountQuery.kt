package yhsb.qb.net.protocol

import yhsb.base.xml.Attribute
import yhsb.base.xml.ToXml

/** 养老个人账户查询单 */
class AccountQuery(
    idCard: String
) : ClientSql(
    "F00.01.03",
    "F03.01.19",
    "( AC01.AAC002 = &apos;${idCard}&apos;)"
) {
    class Item(
        /** 账户ID */
        @Attribute("aac001")
        val id: String,

        @Attribute("aac003")
        val name: String,

        @Attribute("aac002")
        val idCard: String,

        @Attribute("aac006")
        val birthDay: String,

        @Attribute("aab004")
        val companyName: String,

        @Attribute("sab100")
        val companyCode: String
    ) : ToXml
}
package yhsb.qb.net.protocol

import yhsb.base.xml.AttrNode
import yhsb.base.xml.Attribute
import yhsb.base.xml.ToXml

/** 参保人员查询统计 - 基金转入记录 */
class JoinedPersonTransferQuery(
    id: String,
    agencyCode: String
) : AddSql(
    "F27.00.01",
    "F27.02.07",
    "a.aac001 = &apos;${id}&apos;",
    1,
    500
) {
    @AttrNode("para", "aab034")
    val agencyCode = agencyCode

    class Item(
        @Attribute("aac002")
        val idCard: String,

        @Attribute("aac003")
        val name: String,

        @Attribute("aac004")
        val sex: String,

        @Attribute("aac005")
        val nation: String,

        @Attribute("aac008")
        val sbState: SbState,

        @Attribute("aac072")
        val agencyNameBeforeTransfer: String,

        /** 记账标志 */
        @Attribute("aae112")
        val bookMark: BookMark,
    ) : ToXml
}
package yhsb.qb.net.protocol

import yhsb.base.xml.Attribute
import yhsb.base.xml.ToXml

/** 省内参保人员查询 */
class JoinedPersonInProvinceQuery(
    idCard: String
) : ClientSql(
    "F00.01.03",
    "F27.06",
    "( aac002 = &apos;$idCard&apos;)"
) {
    data class Item(
        /** 个人编号 */
        @Attribute("sac100")
        val pid: String,

        @Attribute("aac002")
        val idCard: String,

        @Attribute("aac003")
        val name: String,

        @Attribute("aac008")
        val sbState: SbState,

        @Attribute("aac031")
        val cbState: CbState,

        @Attribute("sac007")
        val jfKind: JfKind,

        /** 社保机构名称 */
        @Attribute("aab300")
        val agencyName: String,

        /** 单位编号 */
        @Attribute("sab100")
        val companyCode: String
    ) : ToXml
}
package yhsb.qb.net.protocol

import yhsb.base.xml.Attribute
import yhsb.base.xml.ToXml
import java.math.BigDecimal

/** 查询税务推送 特殊缴费信息 */
class TaxPushSpecialPayInfoQuery : Query(
    "F00.01.03",
    "F02.08.03"
) {
    class Item(
        /** 征集通知流水号 */
        @Attribute("aaz288")
        val serialNumber: String,

        /** 单位名称 */
        @Attribute("aab004")
        val companyName: String,

        /** 单位编号 */
        @Attribute("aab001")
        val companyCode: String,

        /** 社保应缴金额合计 */
        @Attribute("yije")
        val shouldPaySum: BigDecimal,

        /** 税务实缴金额合计 */
        @Attribute("sjje")
        val actualPaySum: BigDecimal,

        @Attribute("aae015")
        val memo: String
    ) : ToXml
}
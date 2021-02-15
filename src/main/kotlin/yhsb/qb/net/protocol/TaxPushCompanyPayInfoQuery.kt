package yhsb.qb.net.protocol

import yhsb.base.xml.Attribute
import yhsb.base.xml.ToXml
import java.math.BigDecimal

/** 查询税务推送 单位缴费信息 */
class TaxPushCompanyPayInfoQuery : Query(
    "F00.01.03",
    "F02.08.01"
) {
    class Item(
        /** 单位名称 */
        @Attribute("aab004")
        val companyName: String,

        /** 单位编号 */
        @Attribute("sab100")
        val companyCode: String,

        /** 征集通知流水号 */
        @Attribute("aaz288")
        val serialNumber: String,

        /** 费款期起 */
        @Attribute("aae041")
        val periodStartTime: String,

        /** 费款期止 */
        @Attribute("aae042")
        val periodEndTime: String,

        /** 单位实缴汇总 */
        @Attribute("aae063")
        val companyTotalActualPayment: BigDecimal,

        /** 个人实缴汇总 */
        @Attribute("aae064")
        val personalTotalActualPayment: BigDecimal,

        /** 实缴滞纳金 */
        @Attribute("aae057")
        val overdueFine: BigDecimal,

        /** 实缴利息 */
        @Attribute("aae057")
        val interest: BigDecimal
    ) : ToXml
}
package yhsb.qb.net.protocol

import yhsb.base.xml.Attribute
import yhsb.base.xml.ToXml
import java.math.BigDecimal

/** 查询税务推送 灵活就业缴费信息 */
class TaxPushPersonalPayInfoQuery : Query(
    "F00.01.03",
    "F02.08.02"
) {
    class Item(
        /** 缴费开始 */
        @Attribute("aae041")
        val periodStartTime: String,

        /** 缴费截止 */
        @Attribute("aae042")
        val periodEndTime: String,

        /** 缴费月数 */
        @Attribute("jfys")
        val months: Int,

        /** 个人实缴金额 */
        @Attribute("aae082")
        val actualPaySum: BigDecimal,

        /** 个人编号 */
        @Attribute("sac100")
        val pid: String,

        /** 社会保障号码 */
        @Attribute("aac002")
        val idCard: String,

        @Attribute("aac003")
        val name: String,

        /** 税务电子税票号码 */
        @Attribute("aad009")
        val TaxStampNumber: String,

        /** 单位名称 */
        @Attribute("aab004")
        val companyName: String,

    ) : ToXml
}
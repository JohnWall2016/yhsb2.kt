package yhsb.qb.net.protocol

import yhsb.base.xml.Attribute

class NoUKeyWorkerContinueQuery : SimpleClientSql(
    "F00.01.02",
    "F01.02.06.99",
    "(ADS3.SAE114=&apos;0&apos; AND ADS3.AAE013 IS NULL AND ADS3.SAE118=&apos;0&apos; and ads3.functionid=&apos;B06.03.03&apos; and ADS3.AAE013 IS NULL )"
) {
    class Item(
        /** 单位名称 */
        @Attribute("aab004")
        override val companyName: String,

        /** 单位编号 */
        @Attribute("sab100")
        override val companyCode: String,
    ) : CompanyInfo
}
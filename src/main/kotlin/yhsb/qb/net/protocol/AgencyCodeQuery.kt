package yhsb.qb.net.protocol

import yhsb.base.xml.Attribute

/** 社保机构编号查询 */
class AgencyCodeQuery : FunctionId("F00.01.02", "F28.02") {
    class Item(
        @Attribute("aab300")
        val name: String,

        @Attribute("aab034")
        val code: String
    )
}
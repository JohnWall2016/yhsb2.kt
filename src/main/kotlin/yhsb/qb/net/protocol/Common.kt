package yhsb.qb.net.protocol

import yhsb.base.structs.MapField

/** 社保状态 */
class SbState : MapField() {
    override fun getValueMap() = mapOf(
        "1" to "在职",
        "2" to "退休",
        "3" to "终止"
    )
}

/** 参保状态 */
class CbState : MapField() {
    override fun getValueMap() = mapOf(
        "1" to "参保缴费",
        "2" to "暂停缴费",
        "3" to "终止缴费"
    )
}

/** 缴费人员类别 */
class JfKind : MapField() {
    override fun getValueMap() = mapOf(
        "101" to "单位在业人员",
        "102" to "个体缴费",
    )
}

/** 记账标志 */
class BookMark : MapField() {
    override fun getValueMap() = mapOf(
        "0" to "未记账",
        "1" to "已记账",
    )
}

interface CompanyInfo {
    val companyName: String
    val companyCode: String
}


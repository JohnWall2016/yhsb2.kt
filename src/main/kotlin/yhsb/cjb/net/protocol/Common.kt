package yhsb.cjb.net.protocol

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import yhsb.base.struct.MapField
import yhsb.cjb.net.Session

/**
 * 参保状态
 */
class CbState : MapField() {
    override fun getValueMap() = mapOf(
        "0" to "未参保",
        "1" to "正常参保",
        "2" to "暂停参保",
        "4" to "终止参保",
    )
}

/**
 * 缴费状态
 */
class JfState : MapField() {
    override fun getValueMap() = mapOf(
        "1" to "参保缴费",
        "2" to "暂停缴费",
        "3" to "终止缴费",
    )
}

/**
 * 居保状态
 */
interface JbState {
    val cbState: CbState?
    val jfState: JfState?

    val jbState: String
        get() {
            val jfState = jfState?.value
            val cbState = cbState?.value
            return when (jfState) {
                "1" -> when (cbState) {
                    "1" -> "正常缴费人员"
                    else -> "未知类型参保缴费人员: $cbState"
                }
                "2" -> when (cbState) {
                    "2" -> "暂停缴费人员"
                    else -> "未知类型暂停缴费人员: $cbState"
                }
                "3" -> when (cbState) {
                    "1" -> "正常待遇人员"
                    "2" -> "暂停待遇人员"
                    "4" -> "终止参保人员"
                    else -> "未知类型终止缴费人员: $cbState"
                }
                "0", null -> "未参保"
                else -> "未知类型人员: $jfState, $cbState"
            }
        }
}


/**
 * 参保身份
 */
class JbKind : MapField() {
    override fun getValueMap() = codeMap

    companion object {
        val codeMap: BiMap<String, String> = HashBiMap.create(
            mapOf(
                "011" to "普通参保人员",
                "021" to "残一级",
                "022" to "残二级",
                "023" to "残三级",
                "031" to "特困一级",
                "032" to "特困二级",
                "033" to "特困三级",
                "051" to "贫困人口一级",
                "052" to "贫困人口二级",
                "053" to "贫困人口三级",
                "061" to "低保对象一级",
                "062" to "低保对象二级",
                "063" to "低保对象三级",
                "071" to "计生特扶人员",
                "090" to "其他",
            )
        )

        val nameMap: BiMap<String, String> = codeMap.inverse()
    }
}

object Xzqh {
    val codeMap = mapOf(
        "43030200" to "代发虚拟乡镇",
        "43030201" to "长城乡",
        "43030202" to "昭潭街道",
        "43030203" to "先锋街道",
        "43030204" to "万楼街道",
        "43030205" to "（原）鹤岭镇",
        "43030206" to "楠竹山镇",
        "43030207" to "姜畲镇",
        "43030208" to "鹤岭镇",
        "43030209" to "城正街街道",
        "43030210" to "雨湖路街道",
        "43030211" to "（原）平政路街道",
        "43030212" to "云塘街道",
        "43030213" to "窑湾街道",
        "43030214" to "（原）窑湾街道",
        "43030215" to "广场街道",
        "43030216" to "（原）羊牯塘街道)"
    )

    val regExps = listOf(
        "湘潭市雨湖区((.*?乡)(.*?社区)).*",
        "湘潭市雨湖区((.*?乡)(.*?村)).*",
        "湘潭市雨湖区((.*?乡)(.*?政府机关)).*",
        "湘潭市雨湖区((.*?街道)办事处(.*?社区)).*",
        "湘潭市雨湖区((.*?街道)办事处(.*?政府机关)).*",
        "湘潭市雨湖区((.*?镇)(.*?社区)).*",
        "湘潭市雨湖区((.*?镇)(.*?居委会)).*",
        "湘潭市雨湖区((.*?镇)(.*?村)).*",
        "湘潭市雨湖区((.*?街道)办事处(.*?村)).*",
        "湘潭市雨湖区((.*?镇)(.*?政府机关)).*",
        "湘潭市雨湖区((.*?街道)办事处(.*))",
    )

    private fun findMatch(name: String): MatchResult? {
        for (re in regExps) {
            val r = Regex(re)
            val m = r.find(name)
            if (m != null) {
                return m
            }
        }
        return null
    }

    fun getDwName(fullName: String): String? = findMatch(fullName)?.groupValues?.get(2)

    fun getCsName(fullName: String): String? = findMatch(fullName)?.groupValues?.get(3)

    fun getDwAndCsName(fullName: String): Pair<String, String>? {
        val m = findMatch(fullName)
        if (m != null) {
            return Pair(m.groupValues[2], m.groupValues[3])
        }
        return null
    }
}

interface XzqhName {
    val czName: String?

    val dwName: String?
        get() = czName?.run { Xzqh.getDwName(this) }

    val csName: String?
        get() = czName?.run { Xzqh.getCsName(this) }

    val dwAndCsName: Pair<String, String>?
        get() = czName?.run { Xzqh.getDwAndCsName(this) }
}

class DfState() : MapField() {
    override fun getValueMap() = mapOf(
        "1" to "正常发放",
        "2" to "暂停发放",
        "3" to "终止发放",
    )

    constructor(value: String) : this() {
        this.value = value
    }
}

class DfType() : MapField() {
    override fun getValueMap() = mapOf(
        "801" to "独生子女",
        "802" to "乡村教师",
        "803" to "乡村医生",
        "807" to "电影放映",
    )

    constructor(value: String) : this() {
        this.value = value
    }
}

/** 代发支付类型 */
class DfPayType() : MapField() {
    override fun getValueMap() = mapOf(
        "DF0001" to "独生子女",
        "DF0002" to "乡村教师",
        "DF0003" to "乡村医生",
        "DF0007" to "电影放映员",
    )

    constructor(value: String) : this() {
        this.value = value
    }
}

class BankType : MapField() {
    override fun getValueMap() = mapOf(
        "LY" to "中国农业银行",
        "ZG" to "中国银行",
        "JS" to "中国建设银行",
        "NH" to "农村信用合作社",
        "YZ" to "邮政",
        "JT" to "交通银行",
        "GS" to "中国工商银行",
    )
}

/** 支付业务类型 */
class PayType : MapField() {
    override fun getValueMap() = mapOf(
        "F10004" to "重复缴费退费",
        "F10006" to "享受终止退保",
        "F10007" to "缴费调整退款",
    )
}

/** 终止参保原因 */
class StopReason : MapField() {
    override fun getValueMap() = mapOf(
        "1401" to "死亡",
        "1406" to "出外定居",
        "1407" to "参加职保",
        "1499" to "其他原因",
        "6401" to "死亡",
        "6406" to "出外定居",
        "6407" to "参加职保",
        "6499" to "其他原因",
    )
}

/** 暂停参保原因 */
class PauseReason : MapField() {
    override fun getValueMap() = mapOf(
        "1201" to "养老待遇享受人员未提供生存证明",
        "1299" to "其他原因暂停养老待遇",
        "6399" to "其他原因中断缴费",
    )
}

/** 缴费类型 */
class JfType : MapField() {
    override fun getValueMap() = mapOf(
        "10" to "正常应缴",
        "31" to "补缴",
    )
}

/** 缴费项目 */
class JfItem : MapField() {
    override fun getValueMap() = mapOf(
        "1" to "个人缴费",
        "3" to "省级财政补贴",
        "4" to "市级财政补贴",
        "5" to "县级财政补贴",
        "11" to "政府代缴",
        "15" to "退捕渔民补助",
    )
}

/** 缴费方式 */
class JfMethod : MapField() {
    override fun getValueMap() = mapOf(
        "2" to "银行代收",
        "3" to "经办机构自收",
    )
}

/** 审核状态 */
class AuditState : MapField() {
    override fun getValueMap() = mapOf(
        "0" to "未审核",
        "1" to "审核通过"
    )
}

enum class CeaseType(private val nameCh: String) {
    PayingPause("缴费暂停"),
    RetiredPause("待遇暂停"),
    PayingStop("缴费终止"),
    RetiredStop("待遇终止");

    override fun toString() = nameCh
}

data class CeaseInfo(
    val type: CeaseType,
    val reason: String,
    val yearMonth: String,
    val auditState: AuditState,
    val auditDate: String?,
    val memo: String,

    var bankName: String? = null
)

fun Session.getPauseInfoByIdCard(idCard: String): CeaseInfo? {
    sendService(RetiredPersonPauseAuditQuery(idCard))
    val rpResult = getResult<RetiredPersonPauseAuditQuery.Item>()
    if (rpResult.isNotEmpty()) {
        return rpResult.first().let {
            CeaseInfo(
                CeaseType.RetiredPause,
                it.reason.toString(),
                it.pauseYearMonth.toString(),
                it.auditState,
                it.auditDate,
                it.memo
            )
        }
    } else {
        sendService(PayingPersonPauseAuditQuery(idCard))
        val ppResult = getResult<PayingPersonPauseAuditQuery.Item>()
        if (ppResult.isNotEmpty()) {
            return ppResult.first().let {
                CeaseInfo(
                    CeaseType.PayingPause,
                    it.reason.toString(),
                    it.pauseYearMonth,
                    it.auditState,
                    it.auditDate,
                    it.memo
                )
            }
        }
    }
    return null
}

fun Session.getStopInfoByIdCard(idCard: String, additionalInfo: Boolean = false): CeaseInfo? {
    sendService(RetiredPersonStopAuditQuery(idCard))
    val rpResult = getResult<RetiredPersonStopAuditQuery.Item>()
    if (rpResult.isNotEmpty()) {
        return rpResult.first().let {
            val bankName = if (additionalInfo) {
                sendService(RetirePersonStopAuditDetailQuery(it))
                val result = getResult<RetirePersonStopAuditDetailQuery.Item>()
                result.first().bankType.name
            } else {
                null
            }
            CeaseInfo(
                CeaseType.RetiredStop,
                it.reason.toString(),
                it.stopYearMonth.toString(),
                it.auditState,
                it.auditDate,
                it.memo,
                bankName
            )
        }
    } else {
        sendService(PayingPersonStopAuditQuery(idCard))
        val ppResult = getResult<PayingPersonStopAuditQuery.Item>()
        if (ppResult.isNotEmpty()) {
            return ppResult.first().let {
                val bankName = if (additionalInfo) {
                    sendService(PayingPersonStopAuditDetailQuery(it))
                    val result = getResult<PayingPersonStopAuditDetailQuery.Item>()
                    result.first().bankType.name
                } else {
                    null
                }
                CeaseInfo(
                    CeaseType.PayingStop,
                    it.reason.toString(),
                    it.stopYearMonth.toString(),
                    it.auditState,
                    it.auditDate,
                    it.memo,
                    bankName
                )
            }
        }
    }
    return null
}
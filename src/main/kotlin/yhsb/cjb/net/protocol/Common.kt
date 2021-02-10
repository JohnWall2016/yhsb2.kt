package yhsb.cjb.net.protocol

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import yhsb.base.util.structs.MapField

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

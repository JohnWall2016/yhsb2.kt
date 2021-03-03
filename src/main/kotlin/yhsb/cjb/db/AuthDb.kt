package yhsb.cjb.db

import com.google.common.base.Strings
import org.ktorm.database.Database
import org.ktorm.entity.*
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import yhsb.base.db.DbSession

interface DataItem {
    var no: Int
    var neighborhood: String?
    var community: String?
    var address: String?
    var name: String?
    var idCard: String?
    var birthDay: String?
    var poverty: String?
    var povertyDate: String?
    var veryPoor: String?
    var veryPoorDate: String?
    var fullAllowance: String?
    var fullAllowanceDate: String?
    var shortAllowance: String?
    var shortAllowanceDate: String?
    var primaryDisability: String?
    var primaryDisabilityDate: String?
    var secondaryDisability: String?
    var secondaryDisabilityDate: String?
    var isDestitute: String?
    var jbKind: String?
    var jbKindFirstDate: String?
    var jbKindLastDate: String?
    var jbState: String?
    var jbStateDate: String?
}

fun <T : DataItem> T.merge(item: RawItem): Boolean {
    var changed = false

    if (neighborhood.isNullOrEmpty() &&
            !item.neighborhood.isEmpty()) {
        neighborhood = item.neighborhood
        changed = true
    }

    if (community.isNullOrEmpty() &&
        !item.community.isEmpty()) {
        community = item.community
        changed = true
    }

    if (address.isNullOrEmpty() &&
        !item.address.isEmpty()) {
        address = item.address
        changed = true
    }

    if (name.isNullOrEmpty() &&
        !item.name.isEmpty()) {
        name = item.name
        changed = true
    }

    if (idCard.isNullOrEmpty() &&
        !item.idCard.isEmpty()) {
        idCard = item.idCard
        changed = true
    }

    if (birthDay.isNullOrEmpty() &&
        !item.birthDay.isEmpty()) {
        birthDay = item.birthDay
        changed = true
    }

    when (item.type) {
        "贫困人口" -> {
            if (poverty.isNullOrEmpty()) {
                poverty = item.detail
                povertyDate = item.date
                changed = true
            }
            if (isDestitute.isNullOrEmpty()) {
                isDestitute = item.type
                changed = true
            }
        }
        "特困人员" -> {
            if (veryPoor.isNullOrEmpty()) {
                veryPoor = item.detail
                veryPoorDate = item.date
                changed = true
            }
            if (isDestitute.isNullOrEmpty()) {
                isDestitute = item.type
                changed = true
            }
        }
        "全额低保人员" -> {
            if (fullAllowance.isNullOrEmpty()) {
                fullAllowance = item.detail
                fullAllowanceDate = item.date
                changed = true
            }
            if (isDestitute.isNullOrEmpty()) {
                isDestitute = "低保对象"
                changed = true
            }
        }
        "差额低保人员" -> {
            if (shortAllowance.isNullOrEmpty()) {
                shortAllowance = item.detail
                shortAllowanceDate = item.date
                changed = true
            }
            if (isDestitute.isNullOrEmpty()) {
                isDestitute = "低保对象"
                changed = true
            }
        }
        "一二级残疾人员" -> {
            if (primaryDisability.isNullOrEmpty()) {
                primaryDisability = item.detail
                primaryDisabilityDate = item.date
                changed = true
            }
        }
        "三四级残疾人员" -> {
            if (secondaryDisability.isNullOrEmpty()) {
                secondaryDisability = item.detail
                secondaryDisabilityDate = item.date
                changed = true
            }
        }
    }

    return changed
}

interface HistoryItem : DataItem, Entity<HistoryItem> {
    companion object : Entity.Factory<HistoryItem>()
}

object HistoryData : Table<HistoryItem>("fphistorydata") {
    val no = int("no").primaryKey().bindTo { it.no }
    val neighborhood = varchar("xzj").bindTo { it.neighborhood }
    val community = varchar("csq").bindTo { it.community }
    val address = varchar("address").bindTo { it.address }
    val name = varchar("name").bindTo { it.name }
    val idCard = varchar("idcard").bindTo { it.idCard }
    val birthDay = varchar("birthDay").bindTo { it.birthDay }
    val poverty = varchar("pkrk").bindTo { it.poverty }
    val povertyDate = varchar("pkrkDate").bindTo { it.povertyDate }
    val veryPoor = varchar("tkry").bindTo { it.veryPoor }
    val veryPoorDate = varchar("tkryDate").bindTo { it.veryPoorDate }
    val fullAllowance = varchar("qedb").bindTo { it.fullAllowance }
    val fullAllowanceDate = varchar("qedbDate").bindTo { it.fullAllowanceDate }
    val shortAllowance = varchar("cedb").bindTo { it.shortAllowance }
    val shortAllowanceDate = varchar("cedbDate").bindTo { it.shortAllowanceDate }
    val primaryDisability = varchar("yejc").bindTo { it.primaryDisability }
    val primaryDisabilityDate = varchar("yejcDate").bindTo { it.primaryDisabilityDate }
    val secondaryDisability = varchar("ssjc").bindTo { it.secondaryDisability }
    val secondaryDisabilityDate = varchar("ssjcDate").bindTo { it.secondaryDisabilityDate }
    val isDestitute = varchar("sypkry").bindTo { it.isDestitute }
    val jbKind = varchar("jbrdsf").bindTo { it.jbKind }
    val jbKindirstDate = varchar("jbrdsfFirstDate").bindTo { it.jbKindFirstDate }
    val jbKindLastDate = varchar("jbrdsfLastDate").bindTo { it.jbKindLastDate }
    val jbState = varchar("jbcbqk").bindTo { it.jbState }
    val jbStateDate = varchar("jbcbqkDate").bindTo { it.jbStateDate }
}
interface MonthItem : DataItem, Entity<MonthItem> {
    companion object : Entity.Factory<MonthItem>()

    var month: String
}

object MonthData : Table<MonthItem>("fpmonthdata") {
    val no = int("no").primaryKey().bindTo { it.no }
    val neighborhood = varchar("xzj").bindTo { it.neighborhood }
    val community = varchar("csq").bindTo { it.community }
    val address = varchar("address").bindTo { it.address }
    val name = varchar("name").bindTo { it.name }
    val idCard = varchar("idcard").bindTo { it.idCard }
    val birthDay = varchar("birthDay").bindTo { it.birthDay }
    val poverty = varchar("pkrk").bindTo { it.poverty }
    val povertyDate = varchar("pkrkDate").bindTo { it.povertyDate }
    val veryPoor = varchar("tkry").bindTo { it.veryPoor }
    val veryPoorDate = varchar("tkryDate").bindTo { it.veryPoorDate }
    val fullAllowance = varchar("qedb").bindTo { it.fullAllowance }
    val fullAllowanceDate = varchar("qedbDate").bindTo { it.fullAllowanceDate }
    val shortAllowance = varchar("cedb").bindTo { it.shortAllowance }
    val shortAllowanceDate = varchar("cedbDate").bindTo { it.shortAllowanceDate }
    val primaryDisability = varchar("yejc").bindTo { it.primaryDisability }
    val primaryDisabilityDate = varchar("yejcDate").bindTo { it.primaryDisabilityDate }
    val secondaryDisability = varchar("ssjc").bindTo { it.secondaryDisability }
    val secondaryDisabilityDate = varchar("ssjcDate").bindTo { it.secondaryDisabilityDate }
    val isDestitute = varchar("sypkry").bindTo { it.isDestitute }
    val jbKind = varchar("jbrdsf").bindTo { it.jbKind }
    val jbKindirstDate = varchar("jbrdsfFirstDate").bindTo { it.jbKindFirstDate }
    val jbKindLastDate = varchar("jbrdsfLastDate").bindTo { it.jbKindLastDate }
    val jbState = varchar("jbcbqk").bindTo { it.jbState }
    val jbStateDate = varchar("jbcbqkDate").bindTo { it.jbStateDate }

    val month = varchar("month").bindTo { it.month }
}

interface RawItem : Entity<RawItem> {
    companion object : Entity.Factory<RawItem>()

    /** 序号 */
    var no: Int

    /** 乡镇街 */
    var neighborhood: String

    /** 村社区 */
    var community: String

    /** 地址 */
    var address: String

    var name: String

    var idCard: String

    var birthDay: String

    /** 人员类型 */
    var type: String

    /** 类型细节 */
    var detail: String

    /** 数据月份 */
    var date: String

    fun update(item: RawItem) {
        neighborhood = item.neighborhood
        community = item.community
        address = item.address
        name = item.name
        idCard = item.idCard
        birthDay = item.birthDay
        type = item.type
        detail = item.detail
        date = item.date
    }
}

object RawData : Table<RawItem>("fprawdata") {
    val no = int("no").primaryKey().bindTo { it.no }
    val neighborhood = varchar("xzj").bindTo { it.neighborhood }
    val community = varchar("csq").bindTo { it.community }
    val address = varchar("address").bindTo { it.address }
    val name = varchar("name").bindTo { it.name }
    val idCard = varchar("idcard").bindTo { it.idCard }
    val birthDay = varchar("birthDay").bindTo { it.birthDay }
    val type = varchar("type").bindTo { it.type }
    val detail = varchar("detail").bindTo { it.detail }
    val date = varchar("date").bindTo { it.date }
}

interface JoinedPerson : Entity<JoinedPerson> {
    companion object : Entity.Factory<JoinedPerson>()

    var idCard: String

    /** 行政区划 */
    var division: String

    /** 户籍性质 */
    var familialType: String

    var name: String

    /** 性别 */
    var sex: String

    var birthDay: String

    /** 参保身份 */
    var jbKind: String

    /** 参保状态 */
    var cbState: String

    /** 缴费状态 */
    var jfState: String

    /** 参保时间 */
    var joinedTime: String
}

object JoinedPersonData : Table<JoinedPerson>("jbrymx") {
    val idCard = varchar("idcard").primaryKey().bindTo { it.idCard }
    val division = varchar("xzqh").bindTo { it.division }
    val familialType = varchar("hjxz").bindTo { it.familialType }
    val name = varchar("name").bindTo { it.name }
    val sex = varchar("sex").bindTo { it.sex }
    val birthDay = varchar("birthDay").bindTo { it.birthDay }
    val jbKind = varchar("cbsf").bindTo { it.jbKind }
    val cbState = varchar("cbzt").bindTo { it.cbState }
    val jfState = varchar("jfzt").bindTo { it.jfState }
    val joinedTime = varchar("cbsj").bindTo { it.joinedTime }
}

val Database.historyData get() = this.sequenceOf(HistoryData)
val Database.monthData get() = this.sequenceOf(MonthData)
val Database.rawData get() = this.sequenceOf(RawData)
val Database.joinedPersonData get() = this.sequenceOf(JoinedPersonData)

object AuthDb2021 : DbSession("jzfp2021")
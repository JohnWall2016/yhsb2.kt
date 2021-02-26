package yhsb.cjb.db

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import yhsb.base.db.DbSession

interface DataItem {
    val no: Int
    val neighborhood: String
    val community: String
    val address: String
    val name: String
    val idCard: String
    val birthDay: String
    val poverty: String
    val povertyDate: String
    val veryPoor: String
    val veryPoorDate: String
    val fullAllowance: String
    val fullAllowanceDate: String
    val shortAllowance: String
    val shortAllowanceDate: String
    val primaryDisability: String
    val primaryDisabilityDate: String
    val secondaryDisability: String
    val secondaryDisabilityDate: String
    val isDestitute: String
    val jbKind: String
    val jbKindFirstDate: String
    val jbKindLastDate: String
    val jbState: String
    val jbStateDate: String
}

interface HistoryItem : DataItem, Entity<HistoryItem>

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
    val month: String
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
    /** 序号 */
    val no: Int

    /** 乡镇街 */
    val neighborhood: String

    /** 村社区 */
    val community: String

    /** 地址 */
    val address: String

    val name: String

    val idCard: String

    val birthDay: String

    /** 人员类型 */
    val type: String

    /** 类型细节 */
    val detail: String

    /** 数据月份 */
    val date: String
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
    val idCard: String

    /** 行政区划 */
    val division: String

    /** 户籍性质 */
    val familialType: String

    val name: String

    /** 性别 */
    val sex: String

    val birthDay: String

    /** 参保身份 */
    val jbKind: String

    /** 参保状态 */
    val cbState: String

    /** 缴费状态 */
    val jfState: String

    /** 参保时间 */
    val joinedTime: String
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
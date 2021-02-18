package yhsb.cjb.db

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import yhsb.base.db.DbSession

interface FpData : Entity<FpData> {
    val no: Int
    val xzj: String
    val csq: String
    val address: String
    val name: String
    val idCard: String
    val birthDay: String
    val pkrk: String
    val pkrkDate: String
    val tkry: String
    val tkryDate: String
    val qedb: String
    val qedbDate: String
    val cedb: String
    val cedbDate: String
    val yejc: String
    val yejcDate: String
    val ssjc: String
    val ssjcDate: String
    val sypkry: String
    val jbrdsf: String
    val jbrdsfFirstDate: String
    val jbrdsfLastDate: String
    val jbcbqk: String
    val jbcbqkDate: String
}

object FpHistoryData : Table<FpData>("fphistorydata") {
    val no = int("no").primaryKey().bindTo { it.no }
    val xzj = varchar("xzj").bindTo { it.xzj }
    val csq = varchar("csq").bindTo { it.csq }
    val address = varchar("address").bindTo { it.address }
    val name = varchar("name").bindTo { it.name }
    val idCard = varchar("idcard").bindTo { it.idCard }
    val birthDay = varchar("birthDay").bindTo { it.birthDay }
    val pkrk = varchar("pkrk").bindTo { it.pkrk }
    val pkrkDate = varchar("pkrkDate").bindTo { it.pkrkDate }
    val tkry = varchar("tkry").bindTo { it.tkry }
    val tkryDate = varchar("tkryDate").bindTo { it.tkryDate }
    val qedb = varchar("qedb").bindTo { it.qedb }
    val qedbDate = varchar("qedbDate").bindTo { it.qedbDate }
    val cedb = varchar("cedb").bindTo { it.cedb }
    val cedbDate = varchar("cedbDate").bindTo { it.cedbDate }
    val yejc = varchar("yejc").bindTo { it.yejc }
    val yejcDate = varchar("yejcDate").bindTo { it.yejcDate }
    val ssjc = varchar("ssjc").bindTo { it.ssjc }
    val ssjcDate = varchar("ssjcDate").bindTo { it.ssjcDate }
    val sypkry = varchar("sypkry").bindTo { it.sypkry }
    val jbrdsf = varchar("jbrdsf").bindTo { it.jbrdsf }
    val jbrdsfFirstDate = varchar("jbrdsfFirstDate").bindTo { it.jbrdsfFirstDate }
    val jbrdsfLastDate = varchar("jbrdsfLastDate").bindTo { it.jbrdsfLastDate }
    val jbcbqk = varchar("jbcbqk").bindTo { it.jbcbqk }
    val jbcbqkDate = varchar("jbcbqkDate").bindTo { it.jbcbqkDate }
}

val Database.historyData get() = this.sequenceOf(FpHistoryData)

object JzfpDb2021 : DbSession("jzfp2021")
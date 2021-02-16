package yhsb.base.datetime

import java.text.SimpleDateFormat
import java.util.*

object DateTime {
    fun format(pattern: String = "yyyyMMdd", date: Date = Date()) = SimpleDateFormat(pattern).format(date)

    fun toDashedDate(date: String, format: String = """^(\d\d\d\d)(\d\d)(\d\d)$"""): String {
        val m = format.toRegex().find(date)
        if (m != null) {
            return "${m.groupValues[1]}-${m.groupValues[2]}-${m.groupValues[3]}"
        } else {
            throw IllegalArgumentException("Invalid date format: $format")
        }
    }

    fun split(date: String, format: String = """^(\d\d\d\d)(\d\d)(\d\d)?"""): Triple<String, String, String> {
        val m = format.toRegex().find(date)
        if (m != null) {
            return Triple(m.groupValues[1], m.groupValues[2], m.groupValues[3])
        } else {
            throw IllegalArgumentException("Invalid date format: $format")
        }
    }
}
package yhsb.base.datetime

import yhsb.base.collection.LinkedNode
import yhsb.base.collection.flattenToLinkedNode

class YearMonth(
    val year: Int,
    val month: Int
) : Comparable<YearMonth> {
    init {
        require(month >= 1 && month <= 12) { "The month must be >= 1 and <=12" }
    }

    fun offset(months: Int): YearMonth {
        val months = months + month
        var y = months / 12
        var m = months % 12
        if (m <= 0) {
            y -= 1
            m += 12
        }
        return YearMonth(y, m)
    }

    fun max(other: YearMonth) = if (this > other) this else other

    fun min(other: YearMonth) = if (this < other) this else other

    override fun compareTo(other: YearMonth): Int {
        return (year * 12 + month) - (other.year * 12 + other.month)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is YearMonth)
            return false
        return compareTo(other) == 0
    }

    override fun hashCode(): Int {
        return year * 12 + month
    }

    override fun toString(): String {
        return String.format("YearMonth(%04d%02d)", year, month)
    }

    companion object {
        fun from(yearMonth: Int) = YearMonth(yearMonth / 100, yearMonth % 100)
    }
}

class YearMonthRange(
    val start: YearMonth,
    val end: YearMonth
) {
    init {
        require(start <= end) { "start must be less than or equal end" }
    }

    operator fun minus(other: YearMonthRange): LinkedNode<YearMonthRange> {
        return if (other.end < this.start || this.end < other.start) {
            LinkedNode.of(this)
        } else if (other.start <= this.start) {
            if (other.end < this.end) {
                LinkedNode.of(YearMonthRange(other.end.offset(1), this.end))
            } else {
                LinkedNode.empty()
            }
        } else { // other.start > this.start
            if (other.end < this.end) {
                LinkedNode.of(
                    YearMonthRange(this.start, other.start.offset(-1)),
                    YearMonthRange(other.end.offset(1), this.end)
                )
            } else {
                LinkedNode.of(YearMonthRange(this.start, other.start.offset(-1)))
            }
        }
    }

    operator fun minus(those: LinkedNode<YearMonthRange>) = subtract(LinkedNode.of(this), those)

    fun getMonths(): Int = (end.year * 12 + end.month) - (start.year * 12 + start.month) + 1

    override fun toString(): String {
        return "$start-$end"
    }

    companion object {
        fun subtract(these: LinkedNode<YearMonthRange>, those: LinkedNode<YearMonthRange>): LinkedNode<YearMonthRange> {
            var these = these
            those.forEach { that ->
                val list = mutableListOf<LinkedNode<YearMonthRange>>()
                these.forEach {
                    list.add(it - that)
                }
                these = list.flattenToLinkedNode()
            }
            return these
        }
    }
}

fun LinkedNode<YearMonthRange>.getMonths(): Int {
    return fold(0) { acc, ym ->
        acc + ym.getMonths()
    }
}

fun LinkedNode<YearMonthRange>.dropMonths(months: Int): LinkedNode<YearMonthRange> {
    if (isEmpty()) return this
    val firstMonths = this.data.getMonths()
    return if (firstMonths > months) {
        LinkedNode(
            YearMonthRange(
                this.data.start.offset(months),
                this.data.end
            ),
            this.next
        )
    } else if (firstMonths == months) {
        this.next ?: LinkedNode.empty()
    } else {
        this.next?.dropMonths(months - firstMonths) ?: LinkedNode.empty()
    }
}

fun LinkedNode<YearMonthRange>.splitByMonths(months: Int): Pair<LinkedNode<YearMonthRange>, LinkedNode<YearMonthRange>> {
    if (isEmpty()) return Pair(LinkedNode.empty(), LinkedNode.empty())

    val firstMonths = this.data.getMonths()
    return if (firstMonths > months) {
        Pair(
            LinkedNode.of(
                YearMonthRange(
                    this.data.start,
                    this.data.start.offset(months - 1)
                )
            ),
            LinkedNode(
                YearMonthRange(
                    this.data.start.offset(months),
                    this.data.end
                ),
                this.next
            )
        )
    } else if (firstMonths == months) {
        Pair(
            LinkedNode.of(
                YearMonthRange(
                    this.data.start,
                    this.data.end
                ),
            ),
            this.next ?: LinkedNode.empty()
        )
    } else {
        val pair = this.next?.splitByMonths(months - firstMonths)
        Pair(
            LinkedNode(
                YearMonthRange(
                    this.data.start, this.data.end
                ),
                pair?.first
            ),
            pair?.second ?: LinkedNode.empty()
        )
    }
}
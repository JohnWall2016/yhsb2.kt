package yhsb.base.math

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

fun Double.isValidInt(): Boolean = toInt().toDouble() == this

private val chineseNumbers = listOf(
    "零", "壹", "贰", "叁", "肆",
    "伍", "陆", "柒", "捌", "玖",
)

private val chinesePlaces = listOf(
    "", "拾", "佰", "仟", "万", "亿",
)

private val chineseUnits = listOf(
    "元", "角", "分",
)

private val chineseWhole = "整"

fun BigDecimal.toChinseMoney(): String {
    val number = (round(2) * 100).toBigInteger()
    var integer = number div 100
    val fraction = number rem 100

    val length = integer.toString().length
    var ret = ""
    var zero = false
    for (i in (length - 1) downTo 0) {
        val base = 10 pow i
        val quot = integer div base
        if (quot > 0) {
            if (zero) ret += chineseNumbers[0]
            ret += chineseNumbers[quot.toInt()] + chinesePlaces[i % 4]
            zero = false
        } else if (quot == BigInteger.ZERO && ret.isNotEmpty()) {
            zero = true
        }
        if (i >= 4) {
            if (i rem 8 == 0 && ret.isNotEmpty()) {
                ret += chinesePlaces[5]
            } else if (i rem 4 == 0 && ret.isNotEmpty()) {
                ret += chinesePlaces[4]
            }
        }
        integer %= base
        if (integer == BigInteger.ZERO && i != 0) {
            zero = true
            break
        }
    }
    ret += chineseUnits[0]

    if (fraction == BigInteger.ZERO) {
        ret += chineseWhole
    } else {
        val quot = fraction div 10
        val rem = fraction rem 10
        if (rem == BigInteger.ZERO) {
            if (zero) ret += chineseNumbers[0]
            ret += chineseNumbers[quot.toInt()] + chineseUnits[1] + chineseWhole
        } else {
            if (zero || quot == BigInteger.ZERO) {
                ret += chineseNumbers[0]
            }
            if (quot != BigInteger.ZERO) {
                ret += chineseNumbers[quot.toInt()] + chineseUnits[1]
            }
            ret += chineseNumbers[rem.toInt()] + chineseUnits[2]
        }
    }

    return ret
}

fun BigDecimal.round(precision: Int) = setScale(precision, RoundingMode.HALF_UP)

operator fun BigDecimal.times(other: Int) = multiply(other.toBigDecimal())

infix fun BigInteger.div(other: BigInteger) = divide(other)

infix fun BigInteger.div(other: Int) = divide(other.toBigInteger())

infix fun BigInteger.rem(other: Int) = remainder(other.toBigInteger())

operator fun BigInteger.compareTo(other: Int) = compareTo(other.toBigInteger())

infix fun Int.pow(exponent: Int) = toBigInteger().pow(exponent)

infix fun Int.rem(other: Int) = rem(other)
package yhsb.base.text

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
fun String.toByteArray(charsetName: String): ByteArray = (this as java.lang.String).getBytes(charsetName)

interface CharWidthProvider {
    fun getWidth(ch: Char): Int?
}

object ChineseCharWidthProvider : CharWidthProvider {

    class ScatteredChars(val chars: Array<Int>, val width: Int) : CharWidthProvider {
        override fun getWidth(ch: Char): Int? =
            if (chars.contains(ch.toInt())) width else null
    }

    class CharRange(val start: Char, val end: Char, val width: Int) : CharWidthProvider {
        override fun getWidth(ch: Char): Int? =
            if (ch >= start && ch <= end) width else null
    }

    override fun getWidth(ch: Char): Int? {
        for (provider in providers) {
            val w = provider.getWidth(ch)
            if (w != null) return w
        }
        return null
    }

    val providers = listOf(
        CharRange('\u4000', '\u9fa5', 2),
        ScatteredChars(
            arrayOf(
                8211, 8212, 8216, 8217, 8220, 8221,
                8230, 12289, 12290, 12296, 12297, 12298,
                12299, 12300, 12301, 12302, 12303, 12304,
                12305, 12308, 12309, 65281, 65288, 65289,
                65292, 65294, 65306, 65307, 65311
            ), 2
        )
    )
}

operator fun String.times(t: Int): String =
    if (t <= 0) ""
    else {
        StringBuilder().apply {
            (1..t).forEach { _ ->
                append(this@times)
            }
        }.toString()
    }

fun String.fillCount(
    width: Int,
    specialProviders: List<CharWidthProvider> = listOf()
): Int {
    val count = fold(0) { acc, c ->
        var w: Int? = null
        for (p in specialProviders) {
            w = p.getWidth(c)
            if (w != null) break
        }
        acc + (w ?: 1)
    }
    return width - count
}

enum class FillMode {
    Left, Right, Both
}

fun String.fill(
    width: Int,
    fillChar: Char = ' ',
    specialProviders: List<CharWidthProvider> = listOf(ChineseCharWidthProvider),
    mode: FillMode = FillMode.Left,
): String {
    val count = fillCount(width, specialProviders)
    return if (count > 0) {
        StringBuilder().apply {
            val pad = fillChar.toString()
            if (mode == FillMode.Left) {
                append(pad * count)
            } else if (mode == FillMode.Both) {
                append(pad * ((count + 1) / 2))
            }
            append(this@fill)
            if (mode == FillMode.Right) {
                append(pad * count)
            } else if (mode == FillMode.Both) {
                append(pad * (count - ((count + 1) / 2)))
            }
        }.toString()
    } else this
}

fun String.fillLeft(
    width: Int,
    fillChar: Char = ' ',
    specialProviders: List<CharWidthProvider> = listOf(ChineseCharWidthProvider),
) = fill(width, fillChar, specialProviders, FillMode.Left)

fun String.fillRight(
    width: Int,
    fillChar: Char = ' ',
    specialProviders: List<CharWidthProvider> = listOf(ChineseCharWidthProvider),
) = fill(width, fillChar, specialProviders, FillMode.Right)

fun String.bar(
    width: Int,
    fillChar: Char = ' ',
    specialProviders: List<CharWidthProvider> = listOf(ChineseCharWidthProvider)
) = fill(width, fillChar, specialProviders, FillMode.Both)

fun String.insertBeforeLastSubstring(insert: String, substring: String = "."): String {
    val index = lastIndexOf(substring)
    return if (index >= 0) {
        substring(0, index) + insert + substring(index)
    } else {
        this + insert
    }
}

fun String.stripPrefix(prefix: String): String {
    return if (startsWith(prefix)) {
        substring(prefix.length)
    } else this
}

fun String.stripPostfix(postfix: String): String {
    return if (endsWith(postfix)) {
        substring(0, length - postfix.length)
    } else this
}
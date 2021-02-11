package yhsb.base.text

val list = listOf(
    "中国人民", "外国ABC", "ABCefg", "1234"
)

list.forEach {
    println(it.fillLeft(10, '='))
}

list.forEach {
    println(it.fillRight(10, '='))
}

list.forEach {
    println(it.bar(10, '='))
}
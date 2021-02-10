package yhsb.base.net

HttpSocket("124.232.169.221", 80).use {
    println(it.getHttp("/"))
}
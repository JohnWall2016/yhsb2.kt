plugins {
    kotlin("jvm") version "1.4.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    maven { setUrl("https://maven.aliyun.com/nexus/content/groups/public/") }
    maven { setUrl("https://maven.aliyun.com/nexus/content/repositories/jcenter") }
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("com.google.code.gson:gson:2.8.6")

    implementation("com.typesafe:config:1.4.0")

    implementation("com.google.guava:guava:30.1-jre")

    implementation("org.apache.poi:poi:4.1.2")
    implementation("org.apache.poi:poi-ooxml:4.1.2")

    implementation("info.picocli:picocli:4.5.2")

    implementation("org.ktorm:ktorm-core:3.2.0")
    implementation("mysql:mysql-connector-java:8.0.22")

    testImplementation(kotlin("script-runtime"))
}

val execTasks = LinkedHashMap<String, String>()

fun execTask(name: String, description: String, configuration: JavaExec.() -> Unit) {
    execTasks[name] = description

    task(name, JavaExec::class) {
        dependsOn("classes")
        jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
        classpath = sourceSets["main"].runtimeClasspath
        configuration()
    }
}

execTask("cjb.audit", "城居保数据审核程序") {
    main = "yhsb.cjb.app.Audit"
}

execTask("cjb.fetch", "城居保信息检索程序") {
    main = "yhsb.cjb.app.Fetch"
}

execTask("cjb.payment", "财务支付单生成程序") {
    main = "yhsb.cjb.app.Payment"
}

execTask("cjb.treatment", "待遇人员信息核对表格生成程序") {
    main = "yhsb.cjb.app.Treatment"
}

execTask("cjb.delegate", "代发数据导出制表程序") {
    main = "yhsb.cjb.app.Delegate"
}

task("list") {
    doLast {
        val len = execTasks.keys.fold(0) { acc, name ->
            acc.coerceAtLeast(name.length)
        }
        execTasks.forEach { (name, desc) ->
            println("${name.padEnd(len + 1)}$desc")
        }
    }
}
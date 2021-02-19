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

execTask("cjb.audit", "参保审核与参保身份变更程序") {
    main = "yhsb.cjb.app.Audit"
}

execTask("cjb.fetch", "城居保信息查询和更新程序") {
    main = "yhsb.cjb.app.Fetch"
}

task("list") {
    doLast {
        execTasks.forEach { (name, desc) ->
            println("${name.padEnd(12)}$desc")
        }
    }
}
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
    testImplementation(kotlin("script-runtime"))
    testImplementation(kotlin("stdlib"))
    testImplementation(kotlin("reflect"))
}

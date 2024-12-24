
val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    kotlin("jvm") version "2.1.0"
    id("io.ktor.plugin") version "3.0.2"
    kotlin("plugin.serialization") version "2.1.0"
}

group = "io.github.damirdenis-tudor"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // server dependencies
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

    // client dependencies
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    // serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // config
    implementation("io.ktor:ktor-server-config-yaml-jvm")
    implementation("io.ktor:ktor-client-cio-jvm:3.0.2")

    // tests
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

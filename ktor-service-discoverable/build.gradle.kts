
val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("maven-publish")
    id("signing")
}

group = "io.github.damirdenis-tudor"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    // server dependencies
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    // client dependencies
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    // serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // tests
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks["javadoc"])
}

publishing {
    publications {
        create<MavenPublication>("kotlin") {
            groupId = "io.github.damirdenis-tudor"
            artifactId = "ktor-service-discoverable"
            from(components["java"])

            artifact(tasks["kotlinSourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("Ktor Service Discoverable plugin")
                packaging = "jar"
                description.set(
                    "Ktor service registry that support gossip like information dissemination."
                )

                url.set("https://github.com/DamirDenis-Tudor/ktor-server-discovery")

                scm {
                    connection.set("scm:git:https://github.com/DamirDenis-Tudor/ktor-server-discovery.git")
                    developerConnection.set("scm:git:git@github.com:DamirDenis-Tudor/ktor-server-discovery.git")
                    url.set("https://github.com/DamirDenis-Tudor/ktor-server-discovery")
                }

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("DamirDenis-Tudor")
                        name.set("Damir Denis-Tudor")
                        email.set("denis-tudor.damir@student.tuiasi.ro")
                    }
                }
            }
        }
    }
    repositories {
        mavenLocal()
    }
}

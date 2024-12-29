import org.jreleaser.model.Active

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project

plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    id("io.ktor.plugin") version "3.0.2"
    id("org.jreleaser") version "1.15.0"
    id("maven-publish")
    id("signing")
}

group = "io.github.damirdenis-tudor"
version = project.findProperty("releaseVersion") ?: "1.0.0"

val mavenCentralUsername = project.findProperty("mavenCentralUsername")?.toString() ?: ""
val mavenCentralPasswordToken = project.findProperty("mavenCentralPasswordToken")?.toString() ?: ""
val githubToken = project.findProperty("githubToken")?.toString() ?: "no_blank"

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
            groupId = project.group.toString()
            artifactId = project.name
            from(components["java"])

            artifact(tasks["kotlinSourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("Ktor Service Discoverable Plugin")
                packaging = "jar"
                description.set("This plugin enables the discovery of services within a Ktor Service Registry.")

                url.set("https://github.com/DamirDenis-Tudor/ktor-service-discovery/tree/main/ktor-service-discoverable")

                scm {
                    connection.set("scm:git:https://github.com/DamirDenis-Tudor/ktor-service-discovery.git")
                    developerConnection.set("scm:git:git@github.com:DamirDenis-Tudor/ktor-service-discovery.git")
                    url.set("https://github.com/DamirDenis-Tudor/ktor-service-discovery")
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
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

signing {
    sign(publishing.publications["kotlin"])
}

jreleaser {
    release {
        github {
            token = githubToken
        }
        project {
            name = "ktor-service-discoverable"
            description.set("Ktor Service Discoverable plugin")
            copyright.set("Damir Denis-Tudor")
        }
        deploy {
            maven {
                mavenCentral {
                    create("sonatype") {
                        active = Active.RELEASE
                        url = "https://central.sonatype.com/api/v1/publisher"

                        snapshotSupported = true

                        setAuthorization("BEARER")
                        username = mavenCentralUsername
                        password = mavenCentralPasswordToken

                        stagingRepository(layout.buildDirectory.dir("staging-deploy").get().asFile.toPath().toString())

                        connectTimeout = 20
                        readTimeout = 60
                        sign = false

                        verifyUrl = "https://repo1.maven.org/maven2/{{path}}/{{filename}}"
                        namespace = "io.github.damirdenis-tudor"

                        retryDelay = 60
                        maxRetries = 100
                    }
                }
            }
        }
    }
}

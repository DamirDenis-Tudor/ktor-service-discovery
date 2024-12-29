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

    // testing
    testImplementation("io.ktor:ktor-server-test-host-jvm")
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
            version = project.version.toString()

            from(components["java"])

            artifact(tasks["kotlinSourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("Ktor Service Discoverer Plugin")
                packaging = "jar"
                description.set("This plugin facilitates interaction with service replicas, abstracting service discovery and load balancing mechanisms.")

                url.set("https://github.com/DamirDenis-Tudor/ktor-service-discovery/tree/main/ktor-service-discoverer")

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
    deploy {
        layout.buildDirectory.dir("jreleaser").get().asFile.mkdir()

        project {
            name = "ktor-service-discovery"
            description.set("Ktor Service Discovery")
            copyright.set("Damir Denis-Tudor")
        }

        maven {
            mavenCentral {
                create("sonatype") {

                    active = Active.ALWAYS
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
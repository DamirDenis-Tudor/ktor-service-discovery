import org.jreleaser.gradle.plugin.tasks.JReleaserReleaseTask
import org.jreleaser.model.Active

plugins {
    id("base")
    id("org.jreleaser") version "1.15.0"
}

version = project.findProperty("releaseVersion") ?: "1.0.0"

val githubToken = project.findProperty("githubToken")?.toString() ?: "no_blank"

repositories {
    mavenCentral()
}

jreleaser {
    release {
        github {
            token = githubToken

            changelog {
                enabled = true
                setFormatted("ALWAYS")
                preset = "conventional-commits"
                extraProperties = mapOf("categorizeScopes" to true)
                contributors {
                    enabled = false
                }

                append {
                    setTarget("build/jreleaser/release/CHANGELOG.md")
                    enabled = true
                    content = """
                    - [ktor-service-registry](https://central.sonatype.com/artifact/io.github.damirdenis-tudor/ktor-service-registry/${version})
                    - [ktor-service-discoverer](https://central.sonatype.com/artifact/io.github.damirdenis-tudor/ktor-service-discoverer/${version})
                    - [ktor-service-discoverable](https://central.sonatype.com/artifact/io.github.damirdenis-tudor/ktor-service-discoverer/${version})
                """.trimIndent()
                }
            }
        }

        project {
            name = "ktor-service-discovery"
            description.set("Ktor Service Discovery")
            copyright.set("Damir Denis-Tudor")
        }

        deploy {
            active = Active.NEVER
        }
    }
}

subprojects {
    tasks.withType<JReleaserReleaseTask> {
        this.isEnabled = false
    }
}
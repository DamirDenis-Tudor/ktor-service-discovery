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
        }
        project {
            name = "ktor-service-discovery"
            description.set("Ktor Service Discovery")
            copyright.set("Damir Denis-Tudor")
        }
        deploy{
            active = Active.NEVER
        }
    }
}

subprojects {
    tasks.withType<JReleaserReleaseTask> {
        this.isEnabled = false
    }
}

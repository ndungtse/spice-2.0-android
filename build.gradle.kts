import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("com.android.application") version "8.13.0" apply false
    id("com.android.library") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
}

ktlint {
    version = "1.8.0"
    android = true
    ignoreFailures = false

    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.HTML)
    }
}

val gitRootPath = providers
    .exec {
        commandLine("git", "rev-parse", "--show-toplevel")
    }.standardOutput.asText
    .map { it.trim() }

tasks.register<Copy>("installGitHooks") {
    description = "Copies git hooks from Spice-SL/scripts to .git/hooks"
    group = "git hooks"

    from(layout.projectDirectory.dir("Spice-SL/scripts/pre-commit"))
    into(gitRootPath.map { file("$it/.git/hooks") })

    eachFile {
        permissions {
            unix(0b111101101)
        }
    }

    outputs.upToDateWhen { false }
}

project(":app") {
    afterEvaluate {
        tasks.named("preBuild") {
            dependsOn(rootProject.tasks.named("installGitHooks"))
        }
    }
}

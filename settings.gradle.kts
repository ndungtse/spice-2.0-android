pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "spice_sl_mobile"
include(":app")
include(":analytics")
project(":app").projectDir = file("Spice-SL/app")
project(":analytics").projectDir = file("Spice-SL/analytics")

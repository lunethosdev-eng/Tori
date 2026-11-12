pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "Pokke"
include(":app")

include(":bitchord-shared", ":bitchord-sharedui")
project(":bitchord-shared").projectDir = file("integration/bitchord/shared")
project(":bitchord-sharedui").projectDir = file("integration/bitchord/sharedUi")

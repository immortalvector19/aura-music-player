pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AuraMusicPlayer"

include(":app")
include(":core:model")
include(":core:database")
include(":core:data")
include(":core:playback")
include(":core:designsystem")
include(":core:ui")
include(":feature:library")
include(":feature:player")
include(":feature:equalizer")
include(":feature:settings")
include(":feature:widget")

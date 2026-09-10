rootProject.name = "AiShortCut"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":shared")

// core
include(
    ":core:common",
    ":core:network",
    ":core:designsystem",
    ":core:translate",
    ":core:database"
)

// data
include(
    ":data:home",
    ":data:hftrending",
    ":data:githubtrending",
    ":data:newsfeed",
    ":data:openrouter",
    ":data:saved"
)

// presentation
include(
    ":presentation:main",
    ":presentation:feed",
    ":presentation:home",
    ":presentation:discover",
    ":presentation:saved",
    ":presentation:detail"
)

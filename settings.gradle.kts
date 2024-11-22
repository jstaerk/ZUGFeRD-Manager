rootProject.name = "OpenIndex-ZUGFeRD"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            @Suppress("UnstableApiUsage")
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
    @Suppress("UnstableApiUsage")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()

        // Required by compose-webview-multiplatform
        // https://github.com/KevinnZou/compose-webview-multiplatform#multiplatform
        //maven("https://jogamp.org/deployment/maven")
    }
}

include(":manager")
project(":manager").name = "ZUGFeRD-Manager"

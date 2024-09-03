pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "8.6.0"
        id("org.jetbrains.kotlin.android") version "1.9.0"
    }
}
//
//dependencyResolutionManagement(fun DependencyResolutionManagement.() {
//    versionCatalogs {
//        create("libs") {
//            from(files("gradle/libs.versions.toml"))
//        }
//    }
//})

rootProject.name = "BluetoothManagerApp"
include(":app")

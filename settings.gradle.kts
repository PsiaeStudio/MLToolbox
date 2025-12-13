pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0" apply true
}


rootProject.name = "MLToolbox"

include("app")
include("core")
include("foundation")
include("foundation:compress")
include("foundation:db")
include("foundation:fs")
include("foundation:io")
include("foundation:native")
include("foundation:network")
include("foundation:domain")
include("foundation:ui")
include("shared")
include("shared:app")
include("shared:data")
include("shared:domain")
include("shared:ui")
include("feature")
include("feature:forge")
include("feature:gamemanager")
include("feature:modmanager")
include("feature:profile")
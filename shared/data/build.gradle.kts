import app.cash.sqldelight.gradle.SqlDelightDatabase

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.protobuf)
}

sqldelight {
    databases {
        create(
            name = "AppDatabase",
            configureAction = Action<SqlDelightDatabase> {
                packageName.set("dev.psiae.mltoolbox.data.sqldelight")
                srcDirs.setFrom("src/main/sqldelight/app/")
                schemaOutputDirectory.set(file("src/main/sqldelight/app/databases/"))
                migrationOutputDirectory.set(file("src/main/sqldelight/app/migrations/"))
            }
        )
        create(
            name = "ProfileDatabase",
            configureAction = Action<SqlDelightDatabase> {
                packageName.set("dev.psiae.mltoolbox.data.sqldelight.user.profile")
                srcDirs.setFrom("src/main/sqldelight/user/profile/")
                schemaOutputDirectory.set(file("src/main/sqldelight/user/profile/databases/"))
                migrationOutputDirectory.set(file("src/main/sqldelight/user/profile/migrations/"))
            }
        )
    }
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
}

dependencies {
    api(project(":foundation:compress"))
    api(project(":foundation:db"))
    api(project(":foundation:fs"))
    api(project(":foundation:io"))
    api(project(":foundation:native"))
    api(project(":foundation:network"))
    api(libs.protobuf.kotlin)
}
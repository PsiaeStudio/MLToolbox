import app.cash.sqldelight.gradle.SqlDelightDatabase
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.compose.compiler)
    alias(libs.plugins.jetbrains.compose.plugin)
    alias(libs.plugins.kotlinx.atomicfu)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.protobuf)
}

group = "dev.psiae"
version = "1.0.1-alpha.5"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

sqldelight {
    databases {
       create(
           name = "Database",
           configureAction = Action<SqlDelightDatabase>{
               packageName.set("dev.psiae.mltoolbox.data")
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
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality

    implementation(libs.jetbrains.compose.foundation)
    implementation(libs.jetbrains.compose.material)
    implementation(libs.jetbrains.compose.material3)
    implementation(libs.jetbrains.compose.desktop.jvm.windows.x64)

    implementation(libs.jna)
    implementation(libs.jna.platform)

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.filekit.core)
    implementation(libs.coil3.compose)

    implementation(libs.zip4j)
    implementation(libs.junrar)
    implementation(libs.sevenzipjbinding)
    implementation(libs.sevenzipjbinding.windows.amd64)

    implementation(libs.log4k)

    implementation(libs.sqldelight.driver)
    implementation(libs.androidx.datastore.core)
    implementation(libs.protobuf.kotlin)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "MLToolbox"
            packageVersion = "1.0.0"
            copyright = "Copyright (C) 2024 - 2025, Psiae."
            licenseFile.set(project.file("LICENSE"))

            modules("jdk.unsupported", "jdk.accessibility", "java.sql")

            windows {
                iconFile.set(project.file("src/main/resources/drawable/icon_manorlords_logo_text.ico"))
            }

            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
        }

        buildTypes.release.proguard {
            version.set("7.8.0")
            configurationFiles.from(project.file("proguard-rules.pro"))
            isEnabled.set(true)
            obfuscate.set(true)
            joinOutputJars.set(true)
        }
    }
}

afterEvaluate {
    tasks.withType<AbstractJPackageTask>().forEach { task ->
        if (
            task.name.startsWith("create") &&
            task.name.endsWith("Distributable")
        ) {
            task.doLast {
                project.layout.projectDirectory.dir("resources").dir("common").file("LICENSE.txt").asFile
                    .copyTo(
                        target = task.destinationDir.get().dir(task.packageName.get()).file("LICENSE.txt").asFile,
                        // we expect that the dir is empty
                        overwrite = false
                    )
                project.layout.projectDirectory.dir("resources").file("AppManifest.json").asFile
                    .copyTo(
                        target = task.destinationDir.get().dir(task.packageName.get()).dir("MLToolboxApp").file("AppManifest.json").asFile,
                        // we expect that the dir is empty
                        overwrite = false
                    )
            }
        }
    }
}

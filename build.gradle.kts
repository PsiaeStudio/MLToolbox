import app.cash.sqldelight.gradle.SqlDelightDatabase
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight") version "2.1.0"
    id("com.google.protobuf") version "0.9.5"
}

group = "dev.psiae"
version = "1.0.1-alpha.4"

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
           name ="Database",
           configureAction = Action<SqlDelightDatabase>{
               packageName.set("dev.psiae.mltoolbox.data")
           }
       )
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.33.0"
    }
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.material)
    implementation(compose.material3)
    implementation(compose.desktop.currentOs)

    implementation("net.java.dev.jna:jna:5.18.0")
    implementation("net.java.dev.jna:jna-platform:5.18.0")

    implementation("io.github.vinceglb:filekit-core:0.8.7")
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")

    // zip
    implementation("net.lingala.zip4j:zip4j:2.11.5")

    // rar
    implementation("com.github.junrar:junrar:7.5.5")

    // zip, rar5, 7z
    implementation("net.sf.sevenzipjbinding:sevenzipjbinding:16.02-2.01")
    implementation("net.sf.sevenzipjbinding:sevenzipjbinding-windows-amd64:16.02-2.01")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

    // log
    implementation("de.peilicke.sascha:log4k:1.5.2")


    /// should we use version catalogs ?

    // repo
    implementation("org.mobilenativefoundation.store:store5:5.1.0-alpha07")

    // sqlite
    implementation("app.cash.sqldelight:sqlite-driver:2.1.0")

    // datastore
    implementation("androidx.datastore:datastore-core:1.1.7")

    implementation("com.google.protobuf:protobuf-kotlin:4.33.0")
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

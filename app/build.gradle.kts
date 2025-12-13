import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.compose.compiler)
    alias(libs.plugins.jetbrains.compose.plugin)
    alias(libs.plugins.kotlinx.atomicfu)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":foundation"))
    implementation(project(":shared"))
    implementation(project(":feature:modmanager"))
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "MLToolbox"
            packageVersion = "1.0.0"
            copyright = "Copyright (C) 2024 - 2025, Psiae."
            licenseFile.set(rootProject.file("LICENSE"))

            modules("jdk.unsupported", "jdk.accessibility", "java.sql")

            windows {
                iconFile.set(project.file("src/main/resources/drawable/icon_manorlords_logo_text.ico"))
            }

            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
        }

        buildTypes.release.proguard {
            version.set("7.8.0")
            configurationFiles.from(rootProject.file("proguard-rules.pro"))
            isEnabled.set(true)
            obfuscate.set(true)
            optimize.set(true)
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
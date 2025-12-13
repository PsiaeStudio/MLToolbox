group = "dev.psiae"
version = "1.0.1-alpha.6"

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

allprojects {
    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper> {
        kotlin {
            jvmToolchain {
                languageVersion = JavaLanguageVersion.of(24)
            }
            compilerOptions {
                optIn.add("kotlin.uuid.ExperimentalUuidApi")
                optIn.add("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
                optIn.add("androidx.compose.foundation.ExperimentalFoundationApi")
                optIn.add("androidx.compose.ui.ExperimentalComposeUiApi")
            }
        }

        java {
            toolchain {
                languageVersion = JavaLanguageVersion.of(24)
            }
        }
    }
}
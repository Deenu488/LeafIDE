import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("build-logic.root-project")
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.com.android.library) apply false
}

fun Project.configureBaseExtension() {
    extensions.findByType(BaseExtension::class)?.run {
        compileSdkVersion(Versions.compileSdkVersion)
        buildToolsVersion = Versions.buildToolsVersion

        defaultConfig {
            val gitCommitCount = "git rev-list --count HEAD".runCommand().toInt()
            val gitCommitId = "git rev-parse --short HEAD".runCommand()
            val appVersionName = "${Versions.versionName}.r${gitCommitCount}.${gitCommitId}"

            minSdk = Versions.minSdkVersion
            targetSdk = Versions.targetSdkVersion
            versionCode = gitCommitCount
            versionName = appVersionName
        }

        buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
            }
            getByName("release") {
                isMinifyEnabled = false
            }
        }

        compileOptions {
            sourceCompatibility = Versions.javaVersion
            targetCompatibility = Versions.javaVersion
        }
    }
}

fun Project.configureKotlinExtension() {
    extensions.findByType(KotlinAndroidProjectExtension::class)?.run {
        jvmToolchain(Versions.jvmToolchainVersion)
    }
}

fun String.runCommand(): String {
    val parts = this.split("\\s".toRegex())
    val processBuilder = ProcessBuilder(*parts.toTypedArray())
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
    processBuilder.waitFor()
    return processBuilder.inputStream.bufferedReader().readText().trim()
}

subprojects {
    plugins.withId("com.android.application") {
        configureBaseExtension()
    }
    plugins.withId("com.android.library") {
        configureBaseExtension()
    }
    plugins.withId("org.jetbrains.kotlin.android") {
        configureKotlinExtension()
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}
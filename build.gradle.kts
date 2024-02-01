import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.freshplatform"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("net.freshplatform.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<ShadowJar> {
    archiveFileName.set("simpleFilesSync.jar")
    minimize()
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val mainKlass = "Main"

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.google.devtools.ksp").version("1.7.10-1.0.6")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "xyz.lambdagg"
version = "0.1.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // Clikt (command line interface parsing)
    implementation("com.github.ajalt.clikt:clikt:3.5.0")

    // Http server for callback auth
    implementation(platform("org.http4k:http4k-bom:4.27.1.0"))
    implementation("org.http4k:http4k-core")

    // Json parsing
    implementation("com.squareup.moshi:moshi:1.13.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.13.0")
    implementation("com.serjltt.moshi:moshi-lazy-adapters:2.2")
}

application {
    mainClass.set("${project.group}.${project.name.toLowerCase()}.$mainKlass")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
    manifest.attributes(
        "Manifest-Version" to "1.0",
        "Main-Class" to application.mainClass,
    )
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}

plugins {
    alias(libs.plugins.kotlin.jvm)

    id("com.gradleup.shadow") version "8.3.3"
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation(libs.commons.codec)
    implementation(libs.armeria)
    implementation(libs.exposed.core)
    implementation(libs.sqlite.jdbc)

    runtimeOnly(libs.logback)

    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "io.github.dogacel.AppKt"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

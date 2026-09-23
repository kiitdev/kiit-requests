plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "sample.SampleKt"
}

dependencies {
    implementation(project(":kiit-requests"))
    implementation(libs.kotlinx.serialization.json)
    // Needed for runBlocking if the sample calls a suspend function — this module itself doesn't
    // need this on a consumer's classpath, but a real main() calling into it might.
    implementation(libs.kotlinx.coroutines.core)
}

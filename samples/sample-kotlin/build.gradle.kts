plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "sample.SampleKt"
}

dependencies {
    // kiit-requests has no serialization surface, and this sample doesn't touch Files (the one
    // suspend-typed member), so nothing beyond the module itself is needed.
    implementation(project(":kiit-requests"))
}

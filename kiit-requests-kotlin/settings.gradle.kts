pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Lets Gradle auto-provision a JDK toolchain for compiling/testing when only an older JDK is
    // installed locally.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kiit-requests-kotlin"

// Composite build: kiit-inputs isn't published to Maven Central yet, so build it from the local
// checkout instead. Explicit substitution rather than relying on group/version matching, since
// :kiit-inputs (the included subproject) doesn't set `group`/`version` as real Gradle project
// properties (only inside its mavenPublishing { coordinates(...) } block), so default
// group:name-based substitution wouldn't pick it up on its own. Remove this whole block once
// kiit-inputs is actually published and this depends on the real dev.kiit:kiit-inputs artifact.
includeBuild("../../kiit-inputs/kiit-inputs-kotlin") {
    dependencySubstitution {
        substitute(module("dev.kiit:kiit-inputs")).using(project(":kiit-inputs"))
    }
}

// Same situation for kiit-context: not published yet either, built from the local checkout.
includeBuild("../../kiit-context/kiit-context-kotlin") {
    dependencySubstitution {
        substitute(module("dev.kiit:kiit-context")).using(project(":kiit-context"))
    }
}

include(":kiit-requests")
include(":sample-kotlin")

// sample-kotlin stays in the shared ./samples/ folder alongside sample-java/sample-swift, one
// level up from this settings file. sample-java/sample-swift start as empty placeholders, not
// included here until they have real content.
project(":sample-kotlin").projectDir = file("../samples/sample-kotlin")

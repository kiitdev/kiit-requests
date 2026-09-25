plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.skie)
    id("signing")
}

// Single source of truth for the published version, mirroring every other kiit module. Left as
// a placeholder: the starting version and first publish target (GitHub Packages pre-release vs.
// Maven Central stable) are the module owner's call, not something to lock in during scaffolding.
val libraryVersion = "0.0.0"

kotlin {
    jvm {
        compilerOptions {
            // JVM 21 so Kotlin emits PermittedSubclasses for any sealed hierarchies, enabling
            // exhaustive Java pattern-matching `switch`, same as every other kiit KMP module.
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(iosArm64(), iosSimulatorArm64(), iosX64()).forEach {
        it.binaries.framework {
            baseName = "KiitRequests"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // api, not implementation: Request/Response expose kiit-inputs types (Inputs,
            // Meta) directly in their public signatures, so consumers need this on their
            // own compile classpath too. Hardcoded coordinates, not a version-catalog entry,
            // since this is an external kiit library, not part of this repo's own catalog.
            // Resolved from the local checkout via the composite build in settings.gradle.kts
            // until kiit-inputs is actually published, at which point this becomes a normal
            // Maven Central dependency with no other change needed.
            api("dev.kiit:kiit-inputs:0.0.0")

            // Source/Identity/About/Verb/Version/Trace/Content moved here from being defined
            // locally, Request exposes all of these directly. Same local-checkout situation as
            // kiit-inputs above.
            api("dev.kiit:kiit-call:0.0.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Disabled: SKIE's default analytics upload sends git/hardware/project data to Touchlab. Turn
// on only when that's something explicitly wanted, not because it's a default worth keeping.
skie {
    analytics {
        enabled.set(false)
    }
}

android {
    namespace = "kiit.requests"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

/**
 * Store the following in ~/.gradle/gradle.properties
 *
 * signingInMemoryKeyPassword=
 * signingInMemoryKey=
 * signing.gnupg.keyName=
 * signing.gnupg.passphrase=
 *
 * Maven local: ~/.m2/repository/dev/kiit/kiit-requests/
 */
mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    coordinates(
        groupId = "dev.kiit",
        artifactId = "kiit-requests",
        version = libraryVersion,
    )
    pom {
        name = "kiit-requests"
        description = "Protocol-neutral Request/Response modeling for HTTP, CLI, and queue/job " +
            "calls - Kotlin Multiplatform."
        url = "https://kiit.dev"
        licenses {
            license {
                name = "Apache-2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }
        developers {
            developer {
                id = "codehelix"
                name = "CodeHelix"
                url = "https://kiit.dev"
            }
        }
        scm {
            url = "https://github.com/kiitdev/kiit-requests"
            connection = "scm:git:git://github.com/kiitdev/kiit-requests.git"
            developerConnection = "scm:git:ssh://git@github.com/kiitdev/kiit-requests.git"
        }
    }
}

detekt {
    config.setFrom("$projectDir/detekt.yml")
    buildUponDefaultConfig = true
    source.setFrom(
        "src/commonMain/kotlin",
        "src/iosMain/kotlin",
    )
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

// The jvm() target compiles to JVM 21 bytecode (see the jvm{} block above), so jvmTest needs to
// run on a matching JVM, same as every other kiit KMP module.
tasks.named<Test>("jvmTest") {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        },
    )
}

// Read by the release workflow (once one exists) to derive the git tag/GitHub release name from
// the same version published to Maven Central, same convention as every other kiit module.
tasks.register("printVersion") {
    doLast { println(libraryVersion) }
}

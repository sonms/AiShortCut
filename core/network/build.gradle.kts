import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("aishortcut.kmp.library.data")
}

// This module's whole job is to hand out a configured HttpClient, so the Ktor
// client type has to be on its API surface (the data convention plugin only
// wires Ktor as `implementation`). Reach the catalog the same way the
// convention plugins do -- the generated `libs` accessor isn't visible here.
val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

kotlin {
    android {
        namespace = "com.sonms.aishortcut.core.network"
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.findLibrary("ktor-client-core").get())
        }
    }
}

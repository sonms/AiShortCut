import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("aishortcut.kmp.library")
}

// The generated `libs` accessor isn't visible in a module script that applies a
// convention plugin (every other module works around this by wiring catalog
// deps inside the plugin). ML Kit is specific to this one module, so reach the
// catalog through the same findLibrary API the convention plugins use.
val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.findLibrary("mlkit-translate").get())
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}

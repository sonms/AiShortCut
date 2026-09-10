import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("aishortcut.kmp.library")
    id("com.google.devtools.ksp")
    id("androidx.room")
}

// The generated `libs` accessor isn't visible in a module that applies a
// convention plugin (see the project memory), so reach the catalog directly --
// the same trick core:network uses.
val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    android {
        namespace = "com.sonms.aishortcut.core.database"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            // Room runtime + Flow types are on this module's API surface: the
            // DAO returns Flow and data:saved names SavedArticleDao directly.
            api(libs.findLibrary("androidx-room-runtime").get())
            api(libs.findLibrary("kotlinx-coroutines-core").get())
            implementation(libs.findLibrary("androidx-sqlite-bundled").get())
        }
        commonTest.dependencies {
            implementation(libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}

dependencies {
    add("kspAndroid", libs.findLibrary("androidx-room-compiler").get())
    add("kspIosArm64", libs.findLibrary("androidx-room-compiler").get())
    add("kspIosSimulatorArm64", libs.findLibrary("androidx-room-compiler").get())
}

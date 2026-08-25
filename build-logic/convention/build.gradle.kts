plugins {
    `kotlin-dsl`
}

dependencies {
    // compileOnly: these jars only need to be on build-logic's own classpath
    // so the precompiled script plugins below can call `id("...")` — consumer
    // modules get the real plugin version from applying this convention, not
    // from a dependency of build-logic itself.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.kotlin.serialization.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("aishortcutKmpLibrary") {
            id = "aishortcut.kmp.library"
            implementationClass = "AishortcutKmpLibraryConventionPlugin"
        }
        register("aishortcutKmpLibraryCompose") {
            id = "aishortcut.kmp.library.compose"
            implementationClass = "AishortcutKmpLibraryComposeConventionPlugin"
        }
        register("aishortcutKmpLibraryData") {
            id = "aishortcut.kmp.library.data"
            implementationClass = "AishortcutKmpLibraryDataConventionPlugin"
        }
    }
}

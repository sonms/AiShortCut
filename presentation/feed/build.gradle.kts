plugins {
    id("aishortcut.kmp.library.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:designsystem"))
        }
    }
}

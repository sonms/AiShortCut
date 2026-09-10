plugins {
    id("aishortcut.kmp.library.compose")
}

kotlin {
    android {
        namespace = "com.sonms.aishortcut.presentation.feed"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:designsystem"))
        }
    }
}

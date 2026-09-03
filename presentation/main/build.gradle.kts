plugins {
    id("aishortcut.kmp.library.compose")
}

kotlin {
    android {
        namespace = "com.sonms.aishortcut.presentation.main"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:designsystem"))
            implementation(project(":presentation:home"))

            // Composition root: this module assembles the Koin graph, so it sees
            // every data module whose types it binds.
            implementation(project(":data:hfTrending"))
        }
    }
}

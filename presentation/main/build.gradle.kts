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
            implementation(project(":presentation:discover"))
            implementation(project(":presentation:saved"))

            // Composition root: this module assembles the Koin graph, so it sees
            // every data module whose types it binds.
            implementation(project(":data:hfTrending"))
            implementation(project(":data:githubTrending"))
            implementation(project(":data:newsFeed"))
            implementation(project(":data:saved"))
            implementation(project(":core:translate"))
        }
    }
}

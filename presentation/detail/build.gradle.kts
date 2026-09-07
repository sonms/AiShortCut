plugins {
    id("aishortcut.kmp.library.compose")
}

kotlin {
    android {
        namespace = "com.sonms.aishortcut.presentation.detail"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            implementation(project(":core:designsystem"))
            // The sheet renders these domain types directly.
            api(project(":data:hfTrending"))
            api(project(":data:githubTrending"))
            api(project(":data:newsFeed"))
        }
    }
}

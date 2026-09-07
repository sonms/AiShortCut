plugins {
    id("aishortcut.kmp.library.compose")
}

kotlin {
    android {
        namespace = "com.sonms.aishortcut.presentation.discover"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            implementation(project(":core:designsystem"))
            implementation(project(":presentation:detail"))
            implementation(project(":data:hfTrending"))
            implementation(project(":data:githubTrending"))
        }
    }
}

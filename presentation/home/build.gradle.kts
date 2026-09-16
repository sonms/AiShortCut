plugins {
    id("aishortcut.kmp.library.compose")
}

kotlin {
    android {
        namespace = "com.sonms.aishortcut.presentation.home"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:translate"))
            implementation(project(":presentation:feed"))
            implementation(project(":presentation:detail"))
            implementation(project(":data:hftrending"))
            implementation(project(":data:githubtrending"))
            implementation(project(":data:newsfeed"))
            implementation(project(":data:saved"))
            implementation(project(":data:home"))
        }
    }
}

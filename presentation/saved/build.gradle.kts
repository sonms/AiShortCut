plugins {
    id("aishortcut.kmp.library.compose")
}

kotlin {
    android {
        namespace = "com.sonms.aishortcut.presentation.saved"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            implementation(project(":core:designsystem"))
            implementation(project(":presentation:detail"))
            implementation(project(":data:saved"))
            implementation(project(":data:newsfeed"))
        }
    }
}

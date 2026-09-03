plugins {
    id("aishortcut.kmp.library.data")
}

kotlin {
    android {
        namespace = "com.sonms.aishortcut.data.githubtrending"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            implementation(project(":core:network"))
        }
    }
}

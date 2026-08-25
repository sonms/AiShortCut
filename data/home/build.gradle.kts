plugins {
    id("aishortcut.kmp.library.data")
}

kotlin {
    android {
        namespace = "com.sonms.aishortcut.data.home"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
        }
    }
}

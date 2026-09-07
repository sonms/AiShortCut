plugins {
    id("aishortcut.kmp.library.data")
}

kotlin {
    android {
        namespace = "com.sonms.aishortcut.data.newsfeed"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            implementation(project(":core:network"))
            implementation(project(":core:translate"))
        }
    }
}

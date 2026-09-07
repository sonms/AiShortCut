plugins {
    id("aishortcut.kmp.library.data")
}

kotlin {
    android {
        namespace = "com.sonms.aishortcut.data.saved"
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":data:newsFeed"))
        }
    }
}

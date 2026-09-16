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
            api(project(":data:newsfeed"))
            api(project(":data:hftrending"))
            // SavedArticleDao / SavedModelDao are constructor params on the public SavedRepository.
            api(project(":core:database"))
        }
    }
}

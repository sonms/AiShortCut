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
            // SavedArticleDao is a constructor param on the public SavedRepository.
            api(project(":core:database"))
        }
    }
}

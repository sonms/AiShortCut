plugins {
    id("aishortcut.kmp.library.data")
}

kotlin {
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

plugins {
    id("aishortcut.kmp.library.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            implementation(project(":core:designsystem"))
            // FeedLanguage / pick() -- EN/KO labels for the sheet's own fixed text.
            implementation(project(":presentation:feed"))
            // The sheet renders these domain types directly.
            api(project(":data:hftrending"))
            api(project(":data:githubtrending"))
            api(project(":data:newsfeed"))
            api(project(":data:openrouter"))
        }
    }
}

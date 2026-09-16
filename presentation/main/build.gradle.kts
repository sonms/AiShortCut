plugins {
    id("aishortcut.kmp.library.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:designsystem"))
            implementation(project(":presentation:home"))
            implementation(project(":presentation:discover"))
            implementation(project(":presentation:saved"))

            // Composition root: this module assembles the Koin graph, so it sees
            // every data module whose types it binds.
            implementation(project(":data:hftrending"))
            implementation(project(":data:githubtrending"))
            implementation(project(":data:newsfeed"))
            implementation(project(":data:openrouter"))
            implementation(project(":data:saved"))
            implementation(project(":data:home"))
            implementation(project(":core:network"))
            implementation(project(":core:translate"))
            implementation(project(":core:database"))
        }
    }
}

plugins {
    id("aishortcut.kmp.library.data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            implementation(project(":core:network"))
        }
    }
}

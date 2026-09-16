plugins {
    id("aishortcut.kmp.library.data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            // HomeVisitDao is a constructor param on the public HomeRepository.
            api(project(":core:database"))
        }
    }
}

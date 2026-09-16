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
            // HomeVisitDao is a constructor param on the public HomeRepository.
            api(project(":core:database"))
        }
    }
}

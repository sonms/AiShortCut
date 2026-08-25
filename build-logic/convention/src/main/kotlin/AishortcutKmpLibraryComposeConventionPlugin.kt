import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Adds the Compose Multiplatform stack on top of the base library convention.
// Applied by any module that has screens, e.g. presentation:home.
class AishortcutKmpLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("aishortcut.kmp.library")
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                    androidResources {
                        enable = true
                    }
                }

                sourceSets.androidMain.dependencies {
                    implementation(libs.findLibrary("compose-uiToolingPreview").get())
                    implementation(libs.findLibrary("compose-uiTooling").get())
                }
                sourceSets.commonMain.dependencies {
                    // api (not implementation): shared/androidApp/iosApp inherit these
                    // so a screen composed here is directly usable by the platform shells.
                    api(libs.findLibrary("compose-runtime").get())
                    api(libs.findLibrary("compose-foundation").get())
                    api(libs.findLibrary("compose-material3").get())
                    api(libs.findLibrary("compose-ui").get())
                    api(libs.findLibrary("compose-components-resources").get())
                    api(libs.findLibrary("compose-uiToolingPreview").get())
                    api(libs.findLibrary("androidx-lifecycle-viewmodelCompose").get())
                    api(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
                }
            }

            dependencies.add("androidRuntimeClasspath", libs.findLibrary("compose-uiTooling").get())
        }
    }
}

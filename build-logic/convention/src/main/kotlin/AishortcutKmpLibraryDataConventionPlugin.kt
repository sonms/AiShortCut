import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Adds Ktor + kotlinx.serialization on top of the base library convention.
// Applied by any module that talks to a network API, e.g. data:home.
class AishortcutKmpLibraryDataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("aishortcut.kmp.library")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(libs.findLibrary("kotlinx-serialization-json").get())
                    implementation(libs.findLibrary("ktor-client-core").get())
                    implementation(libs.findLibrary("ktor-client-contentNegotiation").get())
                    implementation(libs.findLibrary("ktor-client-logging").get())
                    implementation(libs.findLibrary("ktor-serialization-kotlinxJson").get())
                }
                sourceSets.androidMain.dependencies {
                    implementation(libs.findLibrary("ktor-client-okhttp").get())
                }
                sourceSets.iosMain.dependencies {
                    implementation(libs.findLibrary("ktor-client-darwin").get())
                }
                // Every data module tests its repositories against a Ktor
                // MockEngine, so these belong here rather than per-module.
                sourceSets.commonTest.dependencies {
                    implementation(libs.findLibrary("ktor-client-mock").get())
                    implementation(libs.findLibrary("kotlinx-coroutines-test").get())
                }
            }
        }
    }
}

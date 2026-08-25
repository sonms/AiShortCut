import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Base convention: every KMP library module in this project applies just this
// one plugin (id "aishortcut.kmp.library") to get android+iOS targets, the
// standard compileSdk/minSdk, and a framework name derived from its own
// Gradle path -- nothing here needs repeating per-module.
class AishortcutKmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                // expect class / actual class (e.g. PlatformContext) is still Beta;
                // this just opts in so the warning doesn't show on every build.
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }

                val frameworkBaseName = target.path.removePrefix(":")
                    .split(":")
                    .joinToString("") { it.replaceFirstChar(Char::uppercase) }

                listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = frameworkBaseName
                        isStatic = true
                    }
                }

                // "android" isn't a real member of KotlinMultiplatformExtension --
                // AGP registers it as a named extension at apply time, so it has
                // to be looked up by name instead of written as `android { ... }`
                // the way a build.gradle.kts script can.
                (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryTarget>("android") {
                    compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
                    minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()

                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_11)
                    }
                    withHostTest {
                        isIncludeAndroidResources = true
                    }
                    withDeviceTestBuilder {
                        sourceSetTreeName = "test"
                    }.configure {
                        instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    }
                }

                sourceSets.commonTest.dependencies {
                    implementation(libs.findLibrary("kotlin-test").get())
                }
            }
        }
    }
}

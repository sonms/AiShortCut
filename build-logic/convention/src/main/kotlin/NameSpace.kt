import org.gradle.api.Project

// Single source of truth for module namespaces. Derived from the Gradle path,
// so :data:home -> com.sonms.aishortcut.data.home. The base convention plugin
// (aishortcut.kmp.library) applies this to every module, so no build.gradle.kts
// under core:* / data:* / presentation:* sets its own namespace.
fun Project.aishortcutNamespace(): String =
    "com.sonms.aishortcut." + path.removePrefix(":").replace(":", ".")

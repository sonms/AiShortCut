import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

// Precompiled script plugins (the *.gradle.kts files in this source set) don't
// get the generated type-safe `libs.foo.bar` accessors the way a normal
// build.gradle.kts does, so they look the catalog up through this instead.
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

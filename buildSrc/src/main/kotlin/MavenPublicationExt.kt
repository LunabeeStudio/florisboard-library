import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering

private val Project.android: LibraryExtension
    get() = (this as ExtensionAware).extensions.getByName("android") as LibraryExtension

/**
 * Set additional artifacts to upload
 * - sources
 * - javadoc
 * - aar
 *
 * @param project project current project
 */
fun MavenPublication.setAndroidArtifacts(
    project: Project,
    flavorAarSuffix: String? = null,
) {
    val sourceJar by project.tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(project.android.sourceSets.getByName("main").java.srcDirs)
    }

    val javadocJar by project.tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
        from(project.android.sourceSets.getByName("main").java.srcDirs)
    }

    artifact(sourceJar)
    artifact(javadocJar)
    val aarBasePath = "${project.buildDir}/outputs/aar/"
    val filename = "${project.name.lowercase()}${flavorAarSuffix?.let { "-$it" }.orEmpty()}-release.aar"
    artifact("$aarBasePath$filename")
}

/**
 * Set additional artifacts to upload
 * - sources
 * - javadoc
 * - jar
 *
 * @param project project current project
 */
fun MavenPublication.setJavaArtifacts(project: Project) {
    artifact("${project.buildDir}/libs/${project.name}-${project.version}.jar")
    artifact(project.tasks.named("sourcesJar"))
    artifact(project.tasks.named("javadocJar"))
}

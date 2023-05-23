import org.gradle.configurationcache.extensions.capitalized
import java.net.URI

plugins {
    `maven-publish`
}

val isAndroidLibrary = project.plugins.hasPlugin("android-library")

if (!isAndroidLibrary) {
    project.extensions.configure<JavaPluginExtension>("java") {
        withJavadocJar()
        withSourcesJar()
    }
}

project.extensions.configure<PublishingExtension>("publishing") {
    setupMavenRepository()
    setupPublication()
}

tasks.named("publish${project.name.capitalized()}PublicationToMavenRepository") {
    when {
        isAndroidLibrary -> dependsOn(tasks.named("assembleRelease"))
        else -> dependsOn(tasks.named("jar"))
    }
}

/**
 * Set repository destination depending on [project] and version name.
 * Credentials should be stored in your root gradle.properties, in a non source controlled file.
 *
 * @param project current project
 */
fun PublishingExtension.setupMavenRepository() {
    repositories {
        maven {
            authentication {
                credentials.username = project.properties["artifactory_deployer_release_username"] as? String
                credentials.password = project.properties["artifactory_deployer_release_api_key"] as? String
            }
            url = URI.create("https://artifactory.lunabee.studio/artifactory/florisboard-library-local/")
        }
    }
}

/**
 * Entry point for setting publication detail.
 *
 * @param project current project
 */
fun PublishingExtension.setupPublication() {
    publications { setPublication() }
}

fun PublicationContainer.setPublication() {
    this.create<MavenPublication>(project.name) {
        afterEvaluate {
            setProjectDetails()
            when {
                isAndroidLibrary -> setAndroidArtifacts(project)
                else -> setJavaArtifacts(project)
            }

            setPom()
        }
    }
}

/**
 * Set project details:
 * - groupId will be [ProjectConfig.GROUP_ID]
 * - artifactId will take the name of the current [project]
 * - version will be set in each submodule gradle file
 *
 * @param project project current project
 */
fun MavenPublication.setProjectDetails() {
    groupId = ProjectConfig.GROUP_ID
    artifactId = project.name
    version = project.version.toString()
}

/**
 * Set POM file details.
 *
 * @param project project current project
 */
fun MavenPublication.setPom() {
    pom {
        name.set(project.name.capitalized())
        description.set(project.description)
        url.set(ProjectConfig.LIBRARY_URL)

        scm {
            connection.set("git@github.com:LunabeeStudio/oneSafeRevival_Android.git")
            developerConnection.set("git@github.com:LunabeeStudio/oneSafeRevival_Android.git")
            url.set("https://github.com/LunabeeStudio/oneSafeRevival_Android")
        }

        developers {
            developer {
                id.set("Publisher")
                name.set("Publisher Lunabee")
                email.set("publisher@lunabee.com")
            }
        }

        withXml {
            asNode().appendNode("dependencies").apply {
                fun Dependency.write(scope: String) = appendNode("dependency").apply {
                    appendNode("groupId", group)
                    appendNode("artifactId", name)
                    version?.let { appendNode("version", version) }
                    appendNode("scope", scope)
                }

                configurations["api"].dependencies.forEach { dependency ->
                    dependency.write("implementation")
                }

                configurations["implementation"].dependencies.forEach { dependency ->
                    dependency.write("runtime")
                }
            }
        }
    }
}

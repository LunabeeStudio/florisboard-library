import org.gradle.api.Project

class Versions(project: Project) {
    private val lunabeeVersion: String = "1.0.1"
    private val florisVersion: String = project.properties["version"] as String
    val fullVersion = "$florisVersion-$lunabeeVersion"
}

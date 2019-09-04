package helper

import com.github.lkishalmi.gradle.gatling.GatlingPluginExtension
import com.github.lkishalmi.gradle.gatling.GatlingRunTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import static com.github.lkishalmi.gradle.gatling.GatlingPlugin.GATLING_RUN_TASK_NAME

abstract class GatlingUnitSpec extends GatlingSpec {
    Project project

    GatlingPluginExtension gatlingExt

    GatlingRunTask gatlingRunTask

    def setup() {
        createBuildFolder("/gradle-layout")

        project = ProjectBuilder.builder().withProjectDir(testProjectDir.root).build()
        project.pluginManager.apply 'com.github.lkishalmi.gatling'

        gatlingExt = project.extensions.getByType(GatlingPluginExtension)
        gatlingRunTask = project.tasks.getByName(GATLING_RUN_TASK_NAME) as GatlingRunTask
    }

}

package functional

import com.github.lkishalmi.gradle.gatling.GatlingSpec
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Shared

abstract class GatlingFuncSpec extends GatlingSpec {

    static def GATLING_HOST_NAME_SYS_PROP = "-Dgatling.hostName=HTTP://COMPUTER-DATABASE.GATLING.IO"

    @Shared
    List<File> pluginClasspath

    def setupSpec() {
        def current = getClass().getResource("/").file
        pluginClasspath = [current.replace("classes/test", "classes/main"),
                           current.replace("classes/test", "resources/main")].collect { new File(it) }
    }

    File prepareTest(String layout) {
        createBuildFolder(layout)
        generateBuildScripts()
    }

    BuildResult executeGradle(String task) {
        GradleRunner.create().forwardOutput()
            .withProjectDir(testProjectDir.getRoot())
            .withPluginClasspath(pluginClasspath)
            .withArguments("--stacktrace", GATLING_HOST_NAME_SYS_PROP, task)
            .withDebug(true)
            .build()
    }

    BuildResult executeGradle(String task,String gradleVersion) {
        GradleRunner.create().forwardOutput()
            .withProjectDir(testProjectDir.getRoot())
            .withPluginClasspath(pluginClasspath)
            .withArguments("--stacktrace", GATLING_HOST_NAME_SYS_PROP, task)
            .withDebug(true)
            .withGradleVersion(gradleVersion)
            .forwardOutput()
            .build()
    }
}

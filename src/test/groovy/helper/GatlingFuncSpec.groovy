package helper

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

abstract class GatlingFuncSpec extends GatlingSpec {

    static def GATLING_HOST_NAME_SYS_PROP = "-Dgatling.hostName=HTTP://COMPUTER-DATABASE.GATLING.IO"

    File prepareTest(boolean copyFiles = true) {
        createBuildFolder(copyFiles)
        generateBuildScripts()
    }

    BuildResult executeGradle(String task) {
        GradleRunner.create().forwardOutput()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("--stacktrace", GATLING_HOST_NAME_SYS_PROP, task)
            .withPluginClasspath()
            .withDebug(true)
            .build()
    }

    BuildResult executeGradle(String task, String gradleVersion) {
        GradleRunner.create().forwardOutput()
            .withProjectDir(testProjectDir.getRoot())
            .withArguments("--stacktrace", GATLING_HOST_NAME_SYS_PROP, task)
            .withPluginClasspath()
            .withDebug(true)
            .withGradleVersion(gradleVersion)
            .forwardOutput()
            .build()
    }
}

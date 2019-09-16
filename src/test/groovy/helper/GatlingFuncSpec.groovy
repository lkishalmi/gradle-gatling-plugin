package helper

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

abstract class GatlingFuncSpec extends GatlingSpec {

    static def GATLING_HOST_NAME_SYS_PROP = "-Dgatling.hostName=HTTP://COMPUTER-DATABASE.GATLING.IO"

    void prepareTest(String fixtureDir = "/gradle-layout") {
        createBuildFolder(fixtureDir)
        generateBuildScripts()
    }

    BuildResult executeGradle(String... gradleArgs) {
        GradleRunner.create().forwardOutput()
            .withProjectDir(projectDir.getRoot())
            .withArguments(["--stacktrace", GATLING_HOST_NAME_SYS_PROP] + (gradleArgs as List))
            .withPluginClasspath()
            .withDebug(true)
            .build()
    }
}

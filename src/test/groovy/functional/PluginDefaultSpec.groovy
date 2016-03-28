package functional

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import static org.apache.commons.io.FileUtils.copyDirectory
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PluginDefaultSpec extends Specification {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()

    @Shared
    List<File> pluginClasspath

    File buildFile

    File testProjectBuildDir

    def setupSpec() {
        def current = getClass().getResource("/").file
        pluginClasspath = [current.replace("classes/test", "classes/main"),
                           current.replace("classes/test", "resources/main")].collect { new File(it) }
    }

    def setup() {
        copyDirectory(new File(PluginDefaultSpec.class.getResource("/gradle-layout").file), testProjectDir.root)

        buildFile = testProjectDir.newFile("build.gradle")
        testProjectBuildDir = new File(testProjectDir.root, "build")
    }

    def "should execute all simulations by default"() {
        given:
        buildFile << """
plugins {
    id 'com.github.lkishalmi.gatling'
}
repositories {
    jcenter()
}
"""
        when:
        BuildResult result = GradleRunner.create().forwardOutput()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath(pluginClasspath)
                .withArguments("gatling")
                .build()

        then: "default tasks were executed succesfully"
        result.task(":gatling").outcome == SUCCESS
        result.task(":gatlingClasses").outcome == SUCCESS

        and: "simulations were compiled and resources are copied"
        new File(testProjectBuildDir, "classes/gatling").exists()
        new File(testProjectBuildDir, "resources/gatling").exists()

        and: "all simulations were run"
        def reports = new File(testProjectBuildDir, "reports/gatling")
        reports.exists() && reports.listFiles().size() == 2
    }

    def "should execute single simulation when initiated by rule"() {
        given:
        buildFile << """
plugins {
    id 'com.github.lkishalmi.gatling'
}
repositories {
    jcenter()
}
"""
        when:
        BuildResult result = GradleRunner.create().forwardOutput()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath(pluginClasspath)
                .withArguments("gatling-computerdatabase.Basic1Simulation")
                .build()

        then: "custom task was run successfully"
        result.task(":gatling-computerdatabase.Basic1Simulation").outcome == SUCCESS

        and: "only one simulation was executed"
        new File(testProjectBuildDir, "reports/gatling").listFiles().size() == 1
    }
}

package functional;

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PluginDefaultSpec extends Specification {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()

    @Shared
    List<File> pluginClasspath

    @Shared
    File testResources

    File buildFile

    File testProjectBuildDir

    def setupSpec() {
        pluginClasspath = System.getProperty("pluginClasspath", "").split(":").collect { new File(it) }
        testResources = new File(System.getProperty("testResources"))
    }

    def setup() {
        buildFile = testProjectDir.newFile("build.gradle")
        testProjectBuildDir = new File(testProjectDir.root, "build")
    }

    def "should execute all simulations by default"() {
        given:
        FileUtils.copyDirectory(new File(testResources, "gatling-sample"), testProjectDir.getRoot());

        buildFile << """
plugins {
    id 'com.github.lkishalmi.gatling'
}
repositories {
    jcenter()
}
"""
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath(pluginClasspath)
                .withArguments("gatling")
                .build()

        then: "default tasks were executed succesfully"
        result.task(":gatling").outcome == SUCCESS
        result.task(":gatlingClasses").outcome == SUCCESS

        and: "simulations were compiled"
        new File(testProjectBuildDir, "classes/gatling").exists()

        and: "all simulations were run"
        def reports = new File(testProjectBuildDir, "reports/gatling")
        reports.exists() && reports.listFiles().size() == 2
    }

    def "should execute single simulation when initiated by rule"() {
        given:
        FileUtils.copyDirectory(new File(testResources, "gatling-sample"), testProjectDir.getRoot());

        buildFile << """
plugins {
    id 'com.github.lkishalmi.gatling'
}
repositories {
    jcenter()
}
"""
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath(pluginClasspath)
                .withArguments("gatling-computerdatabase.BasicSimulation")
                .build()

        then: "custom task was run succesfully"
        result.task(":gatling-computerdatabase.BasicSimulation").outcome == SUCCESS
        and: "only one simulation was executed"
        new File(testProjectBuildDir, "reports/gatling").listFiles().size() == 1
    }
}

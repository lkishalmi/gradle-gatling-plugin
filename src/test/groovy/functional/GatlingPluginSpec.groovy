package functional

import groovy.io.FileType
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static org.apache.commons.io.FileUtils.copyDirectory
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class GatlingPluginSpec extends Specification {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()

    @Shared
    List<File> pluginClasspath

    File testProjectBuildDir

    def setupSpec() {
        def current = getClass().getResource("/").file
        pluginClasspath = [current.replace("classes/test", "classes/main"),
                           current.replace("classes/test", "resources/main")].collect { new File(it) }
    }

    def buildDirFromLayout(String layout) {
        copyDirectory(new File(GatlingPluginSpec.class.getResource("/$layout-layout").file), testProjectDir.root)
        testProjectBuildDir = new File(testProjectDir.root, "build")
        testProjectDir.newFile("build.gradle") << """
plugins {
    id 'com.github.lkishalmi.gatling'
}
repositories {
    jcenter()
}
"""
    }

    @Unroll
    def "should execute all simulations by default, layout `#layout`"() {
        setup:
        buildDirFromLayout(layout)

        when:
        BuildResult result = GradleRunner.create().forwardOutput()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath(pluginClasspath)
                .withArguments("gatling")
                .build()

        then: "default tasks were executed succesfully"
        result.task(":gatling").outcome == SUCCESS
        result.task(":gatlingClasses").outcome == SUCCESS

        and: "only layout specific simulations were compiled"
        def classesDir = new File(testProjectBuildDir, "classes/gatling")
        classesDir.eachFileRecurse(FileType.FILES) {
            assert it.name.contains(simulationPart)
        }
        classesDir.exists()

        and: "only layout specific resources are copied"
        def resourcesDir = new File(testProjectBuildDir, "resources/gatling")
        resourcesDir.exists()
        new File(resourcesDir, resourceFile).exists()

        and: "all simulations were run"
        def reports = new File(testProjectBuildDir, "reports/gatling")
        reports.exists() && reports.listFiles().size() == 2

        and: "logback.xml were generated"
        def logback = new File(testProjectBuildDir, "gatling/logback.xml")
        logback.exists()

        where:
        layout      || simulationPart   || resourceFile
        "gradle"    | "1Simulation"     | "search1.csv"
        "gatling"   | "2Simulation"     | "search2.csv"
    }

    @Unroll
    def "should execute only #simulation when initiated by rule, layout `#layout`"() {
        setup:
        buildDirFromLayout(layout)

        when:
        BuildResult result = GradleRunner.create().forwardOutput()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath(pluginClasspath)
                .withArguments("gatling-$simulation")
                .build()

        then: "custom task was run successfully"
        result.task(":gatling-$simulation").outcome == SUCCESS

        and: "only one simulation was executed"
        new File(testProjectBuildDir, "reports/gatling").listFiles().size() == 1

        where:
        layout      || simulation
        "gradle"    | "computerdatabase.Basic1Simulation"
        "gatling"   | "computerdatabase.Basic2Simulation"
    }

    @Unroll
    def "should allow Gatling config override, layout `#layout`"() {
        given:
        buildDirFromLayout(layout)
        and: "override config by disabling reports"
        new File(new File(testProjectDir.root, configLocation), "gatling.conf") << """
gatling {
  data {
    writers = []
  }
}
"""
        when:
        BuildResult result = GradleRunner.create().forwardOutput()
            .withProjectDir(testProjectDir.getRoot())
            .withPluginClasspath(pluginClasspath)
            .withArguments("gatling")
            .build()

        then: "task executed successfully"
        result.task(":gatling").outcome == SUCCESS

        and: "no reports generated"
        !new File(testProjectBuildDir, "reports/gatling").exists()

        where:
        layout      || configLocation
        "gradle"    | "src/gatling/resources/conf"
        "gatling"   | "src/gatling/conf"
    }

    def "should not fail when layout is incorrect"() {
        setup:
        buildDirFromLayout("empty")

        when:
        BuildResult result = GradleRunner.create().forwardOutput()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath(pluginClasspath)
                .withArguments("gatling")
                .build()

        then: "default tasks were executed succesfully"
        result.task(":gatling").outcome == SUCCESS
        result.task(":gatlingClasses").outcome == UP_TO_DATE

        and: "no simulations compiled"
        !new File(testProjectBuildDir, "classes/gatling").exists()

        and: "not resources copied"
        !new File(testProjectBuildDir, "resources/gatling").exists()

        and: "no simulations run"
        !new File(testProjectBuildDir, "reports/gatling").exists()
    }
}

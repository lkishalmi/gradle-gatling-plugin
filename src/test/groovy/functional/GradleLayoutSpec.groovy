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

class GradleLayoutSpec extends Specification {

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

    def createDir(String layout) {
        copyDirectory(new File(GradleLayoutSpec.class.getResource("/$layout-layout").file), testProjectDir.root)
        testProjectBuildDir = new File(testProjectDir.root, "build")
        testProjectDir.newFile("build.gradle")
    }

    def "should execute all simulations by default"() {
        setup:
        createDir("gradle") << """
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

        and: "only gradle-layout simulations were compiled"
        def classesDir = new File(testProjectBuildDir, "classes/gatling")
        classesDir.exists()
        classesDir.eachFileRecurse(FileType.FILES) {
            assert it.name.contains("1Simulation") && !it.name.contains("2Simulation")
        }

        and: "only gradle-layout resources are copied"
        def resourcesDir = new File(testProjectBuildDir, "resources/gatling")
        resourcesDir.exists()
        new File(resourcesDir, "data").list() == ["search1.csv"] as String[]

        and: "all simulations were run"
        def reports = new File(testProjectBuildDir, "reports/gatling")
        reports.exists() && reports.listFiles().size() == 2
    }

    @Unroll
    def "should execute only #simulation when initiated by rule, layout #{layout}"() {
        setup:
        createDir(layout) << """
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
}

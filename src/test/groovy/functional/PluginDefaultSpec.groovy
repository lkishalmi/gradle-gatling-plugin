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

    def setupSpec() {
        pluginClasspath = System.getProperty("pluginClasspath", "").split(":").collect { new File(it) }
        testResources = new File(System.getProperty("testResources"))
    }

    def setup() {
        buildFile = testProjectDir.newFile("build.gradle");
    }

    @Ignore
    def testLoadPlugin() throws IOException {
        given:
        buildFile << """
plugins {
    id 'com.github.lkishalmi.gatling'
}
"""
        when:
        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath(pluginClasspath)
                .withArguments("tasks")
                .build()

        then:
        result.output.contains("gatling - Execute Gatling simulation")
    }

    @Ignore
    def testGatlingCompile() {
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
                .withArguments("clean", "gatlingClasses")
                .build()

        then:
        result.task(":gatlingClasses").outcome == SUCCESS
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
        def testProjectBuildDir = new File(testProjectDir.root, "build")

        then:
        result.task(":gatling").outcome == SUCCESS
        and:
        new File(testProjectBuildDir, "reports/gatling").exists()
    }

}

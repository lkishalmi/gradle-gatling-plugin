package functional;

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
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
        String[] pcp = System.getProperty("pluginClasspath", "").split(":");

        pluginClasspath = new LinkedList<>();
        for (String entry : pcp) {
            pluginClasspath.add(new File(entry));
        }
        testResources = new File(System.getProperty("testResources"));
    }

    def setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
    }

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

}

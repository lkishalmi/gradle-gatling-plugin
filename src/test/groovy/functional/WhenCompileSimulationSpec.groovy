package functional

import helper.GatlingFuncSpec
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.UnexpectedBuildFailure
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class WhenCompileSimulationSpec extends GatlingFuncSpec {

    static def GATLING_CLASSES_TASK_NAME = "gatlingClasses"

    def setup() {
        prepareTest()
    }

    def "should compile"() {
        when:
        BuildResult result = executeGradle(GATLING_CLASSES_TASK_NAME)
        then: "compiled successfully"
        result.task(":$GATLING_CLASSES_TASK_NAME").outcome == SUCCESS
        and: "only layout specific simulations were compiled"
        def classesDir = new File(buildDir, "classes/scala/gatling")
        classesDir.exists()
        and: "only layout specific resources are copied"
        def resourcesDir = new File(buildDir, "resources/gatling")
        resourcesDir.exists()
        new File(resourcesDir, "search.csv").exists()
        and: "main classes are compiled"
        def mainDir = new File(buildDir, "classes/java/main")
        mainDir.exists()
        and: "test classes are compiled"
        def testDir = new File(buildDir, "classes/java/test")
        testDir.exists()
    }

    @Unroll
    def "should not compile without #dir"() {
        setup:
        assert new File(projectDir.root, dir).deleteDir()
        when:
        executeGradle(GATLING_CLASSES_TASK_NAME)
        then:
        thrown(UnexpectedBuildFailure)
        where:
        layout   | dir
        'gradle' | 'src/main'
        'gradle' | 'src/test'
    }

    def "should not compile without required dependencies"() {
        given:
        buildFile.text = """
plugins { id 'com.github.lkishalmi.gatling' }
repositories { jcenter() }
"""
        when:
        executeGradle(GATLING_CLASSES_TASK_NAME)
        then:
        thrown(UnexpectedBuildFailure)
    }
}

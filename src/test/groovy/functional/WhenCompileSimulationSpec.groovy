package functional

import groovy.io.FileType
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.UnexpectedBuildFailure
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class WhenCompileSimulationSpec extends GatlingFuncSpec {

    static def GATLING_CLASSES_TASK_NAME = "gatlingClasses"

    @Unroll
    def "should compile, layout `#layout`"() {
        setup:
        prepareTest(layout)

        when:
        BuildResult result = executeGradle(GATLING_CLASSES_TASK_NAME)

        then: "compiled successfully"
        result.task(":$GATLING_CLASSES_TASK_NAME").outcome == SUCCESS

        and: "only layout specific simulations were compiled"
        def classesDir = new File(testProjectBuildDir, "classes/gatling")
        classesDir.exists()
        and:
        classesDir.eachFileRecurse(FileType.FILES) { assert it.name.contains(simulationPart) }

        and: "only layout specific resources are copied"
        def resourcesDir = new File(testProjectBuildDir, "resources/gatling")
        resourcesDir.exists()
        new File(resourcesDir, resourceFile).exists()

        and: "main classes are compiled"
        def mainDir = new File(testProjectBuildDir, "classes/main")
        mainDir.exists()

        and: "test classes are compiled"
        def testDir = new File(testProjectBuildDir, "classes/test")
        testDir.exists()

        where:
        layout   || simulationPart || resourceFile
        "gradle"  | "1Simulation"   | "search1.csv"
        "gatling" | "2Simulation"   | "search2.csv"
    }

    @Unroll
    def "should not compile without #dir in #layout layout"() {
        setup:
        prepareTest(layout)
        and: "remove test classes"
        assert new File(testProjectDir.root, dir).deleteDir()

        when:
        executeGradle(GATLING_CLASSES_TASK_NAME)

        then:
        thrown(UnexpectedBuildFailure)

        where:
        layout    | dir
        'gradle'  | 'src/main'
        'gradle'  | 'src/test'
        'gatling' | 'src/main'
        'gatling' | 'src/test'
    }

    @Unroll
    def "should not compile without gatling dependencies"() {
        given: "build script without gatling dependencies"
        prepareTest(layout).text = """plugins {
    id 'com.github.lkishalmi.gatling'
}
repositories {
    jcenter()
}
"""

        when:
        executeGradle(GATLING_CLASSES_TASK_NAME)

        then:
        thrown(UnexpectedBuildFailure)

        where:
        layout << [ 'gradle', 'gatling']
    }
}

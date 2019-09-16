package functional

import helper.GatlingFuncSpec
import org.gradle.testkit.runner.BuildResult

import static com.github.lkishalmi.gradle.gatling.GatlingPlugin.GATLING_RUN_TASK_NAME
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class WhenRunSimulationSpec extends GatlingFuncSpec {

    def "should execute all simulations by default"() {
        setup:
        prepareTest()
        when:
        BuildResult result = executeGradle(GATLING_RUN_TASK_NAME)
        then: "default tasks were executed successfully"
        result.task(":$GATLING_RUN_TASK_NAME").outcome == SUCCESS
        result.task(":gatlingClasses").outcome == SUCCESS
        and: "all simulations were run"
        def reports = new File(testProjectBuildDir, "reports/gatling")
        reports.exists() && reports.listFiles().size() == 2
        and: "logs doesn't contain INFO"
        !result.output.split().any { it.contains("INFO") }
    }

    def "should execute only #simulation when initiated by rule"() {
        setup:
        prepareTest()
        when:
        BuildResult result = executeGradle("$GATLING_RUN_TASK_NAME-computerdatabase.BasicSimulation")
        then: "custom task was run successfully"
        result.task(":$GATLING_RUN_TASK_NAME-computerdatabase.BasicSimulation").outcome == SUCCESS
        and: "only one simulation was executed"
        new File(testProjectBuildDir, "reports/gatling").listFiles().size() == 1
        and: "logs doesn't contain INFO"
        !result.output.split().any { it.contains("INFO") }
    }

    def "should allow Gatling config override"() {
        given:
        prepareTest()
        and: "override config by disabling reports"
        new File(new File(testProjectDir.root, "src/gatling/resources"), "gatling.conf") << """
gatling.charting.noReports = true
"""
        when:
        BuildResult result = executeGradle(GATLING_RUN_TASK_NAME)
        then:
        result.task(":$GATLING_RUN_TASK_NAME").outcome == SUCCESS
        and: "no reports generated"
        with(new File(testProjectBuildDir, "reports/gatling").listFiles()) { reports ->
            reports.size() == 2
            reports.find { it.name.startsWith("basicsimulation") } != null
            reports.find { it.name.startsWith("basicsimulation") }.listFiles().collect { it.name } == ["simulation.log"]
            reports.find { it.name.startsWith("advancedsimulationstep03") } != null
            reports.find { it.name.startsWith("advancedsimulationstep03") }.listFiles().collect { it.name } == ["simulation.log"]

        }
    }

    def "should not fail when layout is incorrect"() {
        setup:
        prepareTest(null)
        when:
        BuildResult result = executeGradle(GATLING_RUN_TASK_NAME)
        then: "default tasks were executed successfully"
        result.task(":$GATLING_RUN_TASK_NAME").outcome == SUCCESS
        result.task(":gatlingClasses").outcome == UP_TO_DATE
        and: "no simulations compiled"
        !new File(testProjectBuildDir, "classes/gatling").exists()
        and: "no simulations run"
        with(new File(testProjectBuildDir, "reports/gatling")) {
            it.exists()
            it.list().size() == 0
        }
    }

    def "should not run gatling if no changes to source code"() {
        given:
        prepareTest()
        buildFile << """
gatling { simulations = { include 'computerdatabase/BasicSimulation.scala' } }
"""
        when: '1st time'
        BuildResult result = executeGradle("$GATLING_RUN_TASK_NAME")
        then:
        result.task(":compileGatlingScala").outcome == SUCCESS
        result.task(":$GATLING_RUN_TASK_NAME").outcome == SUCCESS

        when: '2nd time no changes'
        result = executeGradle("$GATLING_RUN_TASK_NAME")
        then:
        result.task(":compileGatlingScala").outcome == UP_TO_DATE
        result.task(":$GATLING_RUN_TASK_NAME").outcome == UP_TO_DATE

        when: '3r time with changes'
        new File(new File(testProjectSrcDir, "computerdatabase"), "BasicSimulation.scala") << """
case class MyClz(str: String) // some fake code to change source file
"""
        result = executeGradle("$GATLING_RUN_TASK_NAME")
        then:
        result.task(":compileGatlingScala").outcome == SUCCESS
        result.task(":$GATLING_RUN_TASK_NAME").outcome == SUCCESS
    }

    //TODO spec to check simulations from different source folders
}

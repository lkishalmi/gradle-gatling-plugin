package functional

import helper.GatlingFuncSpec
import org.gradle.testkit.runner.BuildResult
import spock.lang.Ignore
import spock.lang.Unroll

import static com.github.lkishalmi.gradle.gatling.GatlingPlugin.GATLING_RUN_TASK_NAME
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class WhenRunSimulationSpec extends GatlingFuncSpec {

    def "should execute all simulations by default"() {
        setup:
        prepareTest()
        when:
        BuildResult result = executeGradle(GATLING_RUN_TASK_NAME)
        then: "default tasks were executed succesfully"
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
gatling {
  data {
    writers = []
  }
}"""
        when:
        BuildResult result = executeGradle(GATLING_RUN_TASK_NAME)
        then:
        result.task(":$GATLING_RUN_TASK_NAME").outcome == SUCCESS
        and: "no reports generated"
        !new File(testProjectBuildDir, "reports/gatling").exists()
    }

    def "should not fail when layout is incorrect"() {
        setup:
        prepareTest(false)
        when:
        BuildResult result = executeGradle(GATLING_RUN_TASK_NAME)
        then: "default tasks were executed succesfully"
        result.task(":$GATLING_RUN_TASK_NAME").outcome == SUCCESS
        result.task(":gatlingClasses").outcome == UP_TO_DATE
        and: "no simulations compiled"
        !new File(testProjectBuildDir, "classes/gatling").exists()
        and: "no simulations run"
        !new File(testProjectBuildDir, "reports/gatling").exists()
    }
}

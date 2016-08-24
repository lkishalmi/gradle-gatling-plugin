package functional

import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll

import static com.github.lkishalmi.gradle.gatling.GatlingPlugin.GATLING_RUN_TASK_NAME
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class WhenRunSimulationSpec extends GatlingFuncSpec {

    @Unroll
    def "should execute all simulations by default, layout `#layout`"() {
        setup:
        prepareTest(layout)

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

        where:
        layout    | simulationPart
        "gradle"  | "1Simulation"
        "gatling" | "2Simulation"
    }

    @Unroll
    def "should execute only #simulation when initiated by rule, layout `#layout`"() {
        setup:
        prepareTest(layout)

        when:
        BuildResult result = executeGradle("$GATLING_RUN_TASK_NAME-$simulation")

        then: "custom task was run successfully"
        result.task(":$GATLING_RUN_TASK_NAME-$simulation").outcome == SUCCESS

        and: "only one simulation was executed"
        new File(testProjectBuildDir, "reports/gatling").listFiles().size() == 1

        and: "logs doesn't contain INFO"
        !result.output.split().any { it.contains("INFO") }

        where:
        layout   || simulation
        "gradle"  | "computerdatabase.Basic1Simulation"
        "gatling" | "computerdatabase.Basic2Simulation"
    }

    @Unroll
    def "should allow Gatling config override, layout `#layout`"() {
        given:
        prepareTest(layout)
        and: "override config by disabling reports"
        new File(new File(testProjectDir.root, configLocation), "gatling.conf") << """
gatling {
  data {
    writers = []
  }
}
"""
        when:
        BuildResult result = executeGradle(GATLING_RUN_TASK_NAME)

        then: "task executed successfully"
        result.task(":$GATLING_RUN_TASK_NAME").outcome == SUCCESS

        and: "no reports generated"
        !new File(testProjectBuildDir, "reports/gatling").exists()

        where:
        layout   || configLocation
        "gradle"  | "src/gatling/resources/conf"
        "gatling" | "src/gatling/conf"
    }

    def "should not fail when layout is incorrect"() {
        setup:
        prepareTest("empty")

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

package functional

import helper.GatlingFuncSpec
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.UnexpectedBuildFailure

import static com.github.lkishalmi.gradle.gatling.GatlingPlugin.GATLING_RUN_TASK_NAME
import static org.gradle.testkit.runner.TaskOutcome.*

class WhenRunFailingSimulationSpec extends GatlingFuncSpec {

    def setup() {
        prepareTest()
    }

    def "should execute all simulations even if one fails because of gatling assertions"() {
        given:
        new File(testProjectDir.root, "build.gradle") << """
gatling {
    simulations = ['computerdatabase.FailedSimulation', 'computerdatabase.BasicSimulation']
}
"""
        and: "add incorrect simulation"
        new File(new File(testProjectDir.root, "src/gatling/simulations/computerdatabase"), "FailedSimulation.scala").text = """
package computerdatabase
import io.gatling.core.Predef._
import io.gatling.http.Predef._
class FailedSimulation extends Simulation {
  val httpConf = http.baseUrl("http://qwe.asd.io")
  val scn = scenario("Scenario Name").exec(http("request_1").get("/"))
  setUp(scn.inject(atOnceUsers(1)).protocols(httpConf)).assertions(
    global.successfulRequests.percent.gt(99)
  )
}
"""
        when:
        executeGradle(GATLING_RUN_TASK_NAME)
        then:
        UnexpectedBuildFailure ex = thrown(UnexpectedBuildFailure)
        ex.buildResult.task(":$GATLING_RUN_TASK_NAME").outcome == FAILED
        and: "all simulations were run"
        with(new File(testProjectBuildDir, "reports/gatling")) { reports ->
            reports.exists() && reports.listFiles().size() == 2
            reports.listFiles().find { it.name.startsWith("basicsimulation") } != null
            reports.listFiles().find { it.name.startsWith("failedsimulation") } != null
            new File(reports.listFiles().find { it.name.startsWith("failedsimulation") }, "simulation.log").text.contains("UnknownHostException: qwe.asd.io")
        }
    }

    def "should ignore if simulation without assertions fails with requests"() {
        given:
        new File(testProjectDir.root, "build.gradle") << """
gatling {
    simulations = ['computerdatabase.FailedSimulation', 'computerdatabase.BasicSimulation']
}
"""
        and: "add incorrect simulation"
        new File(new File(testProjectDir.root, "src/gatling/simulations/computerdatabase"), "FailedSimulation.scala").text = """
package computerdatabase
import io.gatling.core.Predef._
import io.gatling.http.Predef._
class FailedSimulation extends Simulation {
  val httpConf = http.baseUrl("http://qwe.asd.io")
  val scn = scenario("Scenario Name").exec(http("request_1").get("/"))
  setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
}
"""
        when:
        def buildResult = executeGradle(GATLING_RUN_TASK_NAME)
        then:
        buildResult.task(":$GATLING_RUN_TASK_NAME").outcome == SUCCESS
        and:
        with(new File(testProjectBuildDir, "reports/gatling")) { reports ->
            reports.exists() && reports.listFiles().size() == 2
            reports.listFiles().find { it.name.startsWith("basicsimulation") } != null
            reports.listFiles().find { it.name.startsWith("failedsimulation") } != null
            new File(reports.listFiles().find { it.name.startsWith("failedsimulation") }, "simulation.log").text.contains("UnknownHostException: qwe.asd.io")
        }
    }

    def "should execute all simulations even if one fails because of gatling runtime"() {
        given:
        new File(testProjectDir.root, "build.gradle") << """
gatling {
    simulations = ['computerdatabase.FailedSimulation', 'computerdatabase.BasicSimulation']
}
"""
        and: "add incorrect simulation"
        new File(new File(testProjectDir.root, "src/gatling/simulations/computerdatabase"), "FailedSimulation.scala").text = """
package computerdatabase
import io.gatling.core.Predef._
import io.gatling.http.Predef._
class FailedSimulation extends Simulation {
}
"""
        when:
        executeGradle(GATLING_RUN_TASK_NAME)
        then:
        UnexpectedBuildFailure ex = thrown(UnexpectedBuildFailure)
        ex.buildResult.task(":$GATLING_RUN_TASK_NAME").outcome == FAILED
        and:
        with(new File(testProjectBuildDir, "reports/gatling")) { reports ->
            reports.exists() && reports.listFiles().size() == 1
            reports.listFiles().find { it.name.startsWith("basicsimulation") } != null
        }
    }
}

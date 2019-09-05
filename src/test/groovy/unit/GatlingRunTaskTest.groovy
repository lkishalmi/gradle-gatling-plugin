package unit

import com.github.lkishalmi.gradle.gatling.GatlingPluginExtension
import helper.GatlingUnitSpec

import static com.github.lkishalmi.gradle.gatling.GatlingPluginExtension.SIMULATIONS_DIR
import static org.apache.commons.io.FileUtils.copyFileToDirectory
import static org.apache.commons.io.FileUtils.moveFileToDirectory

class GatlingRunTaskTest extends GatlingUnitSpec {

    def "should resolve simulations using default filter"() {
        when:
        def gatlingRunSimulations = gatlingRunTask.resolveSimulations()
        then:
        gatlingRunSimulations.size() == 2
        and:
        "computerdatabase.advanced.AdvancedSimulationStep03" in gatlingRunSimulations
        and:
        "computerdatabase.BasicSimulation" in gatlingRunSimulations
    }

    def "should resolve simulations using custom filter"() {
        given:
        project.gatling.simulations = { include "**/*AdvancedSimulation*" }
        when:
        def gatlingRunSimulations = gatlingRunTask.resolveSimulations()
        then:
        gatlingRunSimulations == ["computerdatabase.advanced.AdvancedSimulationStep03"]
    }

    def "should resolve simulations using custom static list"() {
        given:
        project.gatling.simulations = ["computerdatabase.advanced.AdvancedSimulationStep03"]
        when:
        def gatlingRunSimulations = gatlingRunTask.resolveSimulations()
        then:
        gatlingRunSimulations == ["computerdatabase.advanced.AdvancedSimulationStep03"]
    }

    def "should resolve simulations using gatlingRun filter"() {
        given:
        project.gatling.simulations = GatlingPluginExtension.DEFAULT_SIMULATIONS
        and:
        project.gatlingRun.simulations = { include "**/*AdvancedSimulation*" }
        when:
        def gatlingRunSimulations = gatlingRunTask.resolveSimulations()
        then:
        gatlingRunSimulations == ["computerdatabase.advanced.AdvancedSimulationStep03"]
    }

    def "should resolve simulations using gatlingRun static list"() {
        given:
        project.gatling.simulations = GatlingPluginExtension.DEFAULT_SIMULATIONS
        and:
        project.gatlingRun.simulations = ["computerdatabase.advanced.AdvancedSimulationStep03"]
        when:
        def gatlingRunSimulations = gatlingRunTask.resolveSimulations()
        then:
        gatlingRunSimulations == ["computerdatabase.advanced.AdvancedSimulationStep03"]
    }

    def "should fail if extension filter neither closure nor iterable"() {
        given:
        project.gatling.simulations = "qwerty"
        when:
        gatlingRunTask.resolveSimulations()
        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("qwerty")
    }

    def "should fail if gatlingRun filter neither closure nor iterable"() {
        given:
        project.gatlingRun.simulations = "qwerty"
        when:
        gatlingRunTask.resolveSimulations()
        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("qwerty")
    }

    def "should override simulations dirs via sourceSet"() {
        given:
        def overridenSrc = "test/gatling/scala"

        when: 'fake source dirs without simulations'
        project.sourceSets {
            gatling.scala.srcDirs = [overridenSrc]
        }
        then:
        gatlingRunTask.resolveSimulations().size() == 0

        when:
        copyFileToDirectory(new File(testProjectDir.root, "${SIMULATIONS_DIR}/computerdatabase/BasicSimulation.scala"),
            new File(testProjectDir.root, "$overridenSrc/computerdatabase"))
        then:
        gatlingRunTask.resolveSimulations() == ["computerdatabase.BasicSimulation"]
    }

    def "should extend simulations dirs via sourceSet"() {
        given:
        def overridenSrc = "test/gatling/scala"

        when: 'fake source dirs without simulations'
        project.sourceSets {
            gatling.scala.srcDir overridenSrc
        }
        then:
        gatlingRunTask.resolveSimulations().size() == 2

        when: "temporary hide one simulation"
        moveFileToDirectory(new File(testProjectDir.root, "${SIMULATIONS_DIR}/computerdatabase/BasicSimulation.scala"),
            testProjectDir.root, true)
        then:
        gatlingRunTask.resolveSimulations() == ["computerdatabase.advanced.AdvancedSimulationStep03"]

        when:
        moveFileToDirectory(new File(testProjectDir.root, "BasicSimulation.scala"), new File(testProjectDir.root, "$overridenSrc/computerdatabase"), true)
        then:
        gatlingRunTask.resolveSimulations().size() == 2
    }

    def "should fail if extension static list is not in sourceSet"() {
        when: 'fake source dirs without simulations and static list'
        project.sourceSets {
            gatling.scala.srcDirs = ["test/gatling/scala"]
        }
        project.gatling { simulations = ["computerdatabase.BasicSimulation"] }
        then:
        gatlingRunTask.resolveSimulations().size() == 0
    }

    def "should fail if gatlingRun static list is not in sourceSet"() {
        when: 'fake source dirs without simulations and static list'
        project.sourceSets {
            gatling.scala.srcDirs = ["test/gatling/scala"]
        }
        project.gatlingRun { simulations = ["computerdatabase.BasicSimulation"] }
        then:
        gatlingRunTask.resolveSimulations().size() == 0
    }
}

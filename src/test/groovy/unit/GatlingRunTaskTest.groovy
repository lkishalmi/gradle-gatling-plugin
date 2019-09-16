package unit

import com.github.lkishalmi.gradle.gatling.GatlingPluginExtension
import helper.GatlingUnitSpec
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import spock.lang.Unroll

import static com.github.lkishalmi.gradle.gatling.GatlingPluginExtension.SIMULATIONS_DIR
import static org.apache.commons.io.FileUtils.copyFileToDirectory
import static org.apache.commons.io.FileUtils.moveFileToDirectory

class GatlingRunTaskTest extends GatlingUnitSpec {

    def "should resolve simulations using default filter"() {
        when:
        def gatlingRunSimulations = gatlingRunTask.simulationFilesToFQN()
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
        def gatlingRunSimulations = gatlingRunTask.simulationFilesToFQN()
        then:
        gatlingRunSimulations == ["computerdatabase.advanced.AdvancedSimulationStep03"]
    }

    def "should resolve simulations using custom static list"() {
        given:
        project.gatling.simulations = {
            include "computerdatabase/advanced/AdvancedSimulationStep03.scala"
        }
        when:
        def gatlingRunSimulations = gatlingRunTask.simulationFilesToFQN()
        then:
        gatlingRunSimulations == ["computerdatabase.advanced.AdvancedSimulationStep03"]
    }

    def "should resolve simulations using gatlingRun filter"() {
        given:
        project.gatling.simulations = GatlingPluginExtension.DEFAULT_SIMULATIONS
        and:
        project.gatlingRun.simulations = { include "**/*AdvancedSimulation*" }
        when:
        def gatlingRunSimulations = gatlingRunTask.simulationFilesToFQN()
        then:
        gatlingRunSimulations == ["computerdatabase.advanced.AdvancedSimulationStep03"]
    }

    def "should resolve simulations using gatlingRun static list"() {
        given:
        project.gatling.simulations = GatlingPluginExtension.DEFAULT_SIMULATIONS
        and:
        project.gatlingRun.simulations = {
            include "computerdatabase/advanced/AdvancedSimulationStep03.scala"
        }
        when:
        def gatlingRunSimulations = gatlingRunTask.simulationFilesToFQN()
        then:
        gatlingRunSimulations == ["computerdatabase.advanced.AdvancedSimulationStep03"]
    }

    @Unroll
    def "should fail if extension filter is not a closure but #val"() {
        when:
        project.gatling.simulations = val
        then:
        def ex = thrown(GroovyCastException)
        ex.message.contains(val.toString())
        where:
        val << ["", "qwerty", ["1", "2"]]
    }

    @Unroll
    def "should fail if gatlingRun filter not a closure but #val"() {
        when:
        project.gatlingRun.simulations = val
        then:
        def ex = thrown(GroovyCastException)
        ex.message.contains(val.toString())
        where:
        val << ["", "qwerty", ["1", "2"]]
    }

    def "should override simulations dirs via sourceSet"() {
        given:
        def overridenSrc = "test/gatling/scala"

        when: 'using source dirs without simulations'
        project.sourceSets {
            gatling.scala.srcDirs = [overridenSrc]
        }
        then:
        gatlingRunTask.simulationFilesToFQN().size() == 0

        when: 'put simulations into overridden source dir'
        copyFileToDirectory(new File(testProjectDir.root, "${SIMULATIONS_DIR}/computerdatabase/BasicSimulation.scala"),
            new File(testProjectDir.root, "$overridenSrc/computerdatabase"))
        then:
        gatlingRunTask.simulationFilesToFQN() == ["computerdatabase.BasicSimulation"]
    }

    def "should extend simulations dirs via sourceSet"() {
        given:
        def overridenSrc = "test/gatling/scala"

        when: 'source dirs without simulations'
        project.sourceSets {
            gatling.scala.srcDir overridenSrc
        }
        then:
        gatlingRunTask.simulationFilesToFQN().size() == 2

        when: "hide one simulation"
        moveFileToDirectory(new File(testProjectDir.root, "${SIMULATIONS_DIR}/computerdatabase/BasicSimulation.scala"),
            testProjectDir.root, true)
        then:
        gatlingRunTask.simulationFilesToFQN() == ["computerdatabase.advanced.AdvancedSimulationStep03"]

        when: 'move simulation back to overridden source dir'
        moveFileToDirectory(new File(testProjectDir.root, "BasicSimulation.scala"), new File(testProjectDir.root, "$overridenSrc/computerdatabase"), true)
        then:
        gatlingRunTask.simulationFilesToFQN().size() == 2
    }

    def "should not find missing simulations configured via extension static list"() {
        when: 'static list with with missing simulations'
        project.gatling.simulations = {
            include "computerdatabase/BasicSimulation.scala"
            include "some.missing.file"
        }
        then:
        gatlingRunTask.simulationFilesToFQN() == ["computerdatabase.BasicSimulation"]

        when: 'fake source dirs without simulations and static list'
        project.sourceSets {
            gatling.scala.srcDirs = ["missing/gatling"]
        }
        project.gatling.simulations = {
            include "computerdatabase/BasicSimulation.scala"
        }
        then:
        gatlingRunTask.simulationFilesToFQN().size() == 0
    }

    def "should not find missing simulations configured via gatlingRun static list"() {
        when: 'static list with missing simulations'
        project.gatlingRun.simulations = {
            include "computerdatabase/BasicSimulation.scala"
            include "some.missing.file"
        }
        then:
        gatlingRunTask.simulationFilesToFQN() == ["computerdatabase.BasicSimulation"]

        when: 'fake source dirs without simulations and static list'
        project.sourceSets {
            gatling.scala.srcDirs = ["missing/gatling"]
        }
        project.gatlingRun.simulations = {
            include "computerdatabase/BasicSimulation.scala"
        }
        then:
        gatlingRunTask.simulationFilesToFQN().size() == 0
    }


}

package unit


import helper.GatlingUnitSpec

import static com.github.lkishalmi.gradle.gatling.GatlingPluginExtension.SIMULATIONS_DIR
import static org.apache.commons.io.FileUtils.copyFileToDirectory
import static org.apache.commons.io.FileUtils.moveFileToDirectory

class GatlingRunTaskTest extends GatlingUnitSpec {

    def "should resolve simulations using extension filter"() {
        expect:
        def gatlingRunSimulations = gatlingExt.resolveSimulations(gatlingRunTask.simulations)
        gatlingRunSimulations == gatlingExt.resolveSimulations()
        and:
        gatlingRunSimulations.size() == 2
        and:
        "computerdatabase.advanced.AdvancedSimulationStep03" in gatlingRunSimulations
        and:
        "computerdatabase.BasicSimulation" in gatlingRunSimulations
    }

    def "should override simulations filter via extension"() {
        when:
        project.gatling {
            simulations = {
                include "**/*AdvancedSimulation*"
            }
        }
        then:
        def gatlingRunSimulations = gatlingExt.resolveSimulations(gatlingRunTask.simulations)
        gatlingRunSimulations == gatlingExt.resolveSimulations()
        and:
        gatlingRunSimulations == ["computerdatabase.advanced.AdvancedSimulationStep03"]
    }

    def "should override simulations filter via task properties"() {
        when:
        project.gatlingRun.simulations = {
            include "**/*BasicSimulation*"
        }
        then:
        def gatlingRunSimulations = gatlingExt.resolveSimulations(gatlingRunTask.simulations)
        gatlingRunSimulations != gatlingExt.resolveSimulations()
        and:
        gatlingRunSimulations.size() == 1
        and:
        gatlingRunSimulations == ["computerdatabase.BasicSimulation"]
    }

    def "should override simulations dirs via sourceSet"() {
        given:
        def overridenSrc = "test/gatling/scala"

        when: 'fake source dirs without simulations'
        project.sourceSets {
            gatling {
                scala.srcDirs = [overridenSrc]
            }
        }
        then:
        gatlingExt.resolveSimulations().size() == 0

        when:
        copyFileToDirectory(new File(testProjectDir.root, "${SIMULATIONS_DIR}/computerdatabase/BasicSimulation.scala"),
            new File(testProjectDir.root, "$overridenSrc/computerdatabase"))
        then:
        gatlingExt.resolveSimulations() == ["computerdatabase.BasicSimulation"]
    }

    def "should extend simulations dirs via sourceSet"() {
        given:
        def overridenSrc = "test/gatling/scala"

        when: 'fake source dirs without simulations'
        project.sourceSets {
            gatling {
                scala.srcDir overridenSrc
            }
        }
        then:
        gatlingExt.resolveSimulations().size() == 2

        when: "temporary hide one simulation"
        moveFileToDirectory(new File(testProjectDir.root, "${SIMULATIONS_DIR}/computerdatabase/BasicSimulation.scala"),
            testProjectDir.root, true)
        then:
        gatlingExt.resolveSimulations() == ["computerdatabase.advanced.AdvancedSimulationStep03"]

        when:
        moveFileToDirectory(new File(testProjectDir.root, "BasicSimulation.scala"), new File(testProjectDir.root, "$overridenSrc/computerdatabase"), true)
        then:
        gatlingExt.resolveSimulations().size() == 2
    }

    def "should use jvmArgs from extension"() {
        expect:
        gatlingExt.jvmArgs == gatlingRunTask.getJvmArgs()
    }

    def "should override jvmArgs via extension"() {
        when:
        project.gatling {
            jvmArgs = ["-Dname=value"]
        }
        then:
        gatlingRunTask.getJvmArgs() == gatlingExt.jvmArgs
        and:
        gatlingRunTask.getJvmArgs() == ["-Dname=value"]
    }

    def "should override jvmArgs via own properties"() {
        when:
        project.gatlingRun {
            jvmArgs = ["-Dname2=value2"]
        }
        then:
        gatlingRunTask.getJvmArgs() != gatlingExt.jvmArgs
        and:
        gatlingRunTask.getJvmArgs() == ["-Dname2=value2"]
    }
}

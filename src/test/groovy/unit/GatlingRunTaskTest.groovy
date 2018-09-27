package unit

import helper.GatlingUnitSpec

class GatlingRunTaskTest extends GatlingUnitSpec {

    def "should resolve simulations using extension filter"() {
        expect:
        def gatlingRunSimulations = gatlingExt.resolveSimulations(gatlingRunTask.simulations)
        gatlingRunSimulations == gatlingExt.resolveSimulations()
        and:
        gatlingRunSimulations.size() == 2
        and:
        gatlingRunSimulations.any { it.endsWith("AdvancedSimulationStep03") }
        and:
        gatlingRunSimulations.any { it.endsWith("BasicSimulation") }
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
        gatlingRunSimulations.size() == 1
        and:
        gatlingRunSimulations.head().endsWith("AdvancedSimulationStep03")
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
        gatlingRunSimulations.head().endsWith("BasicSimulation")
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

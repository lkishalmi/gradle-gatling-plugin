package com.github.lkishalmi.gradle.gatling

class GatlingRunTaskTest extends GatlingUnitSpec {

    def "should resolve simulations using extension filter"() {
        expect:
        def gatlingRunSimulations = gatlingExt.resolveSimulations(gatlingRunTask.simulations)
        gatlingRunSimulations == gatlingExt.resolveSimulations()
        and:
        gatlingRunSimulations.size() == 2
        and:
        gatlingRunSimulations.any { it.endsWith("Advanced1Simulation") }
        and:
        gatlingRunSimulations.any { it.endsWith("Basic1Simulation") }
    }

    def "should override simulations filter via extension"() {
        when:
        project.gatling {
            simulations = {
                include "**/*Advanced1Simulation*"
            }
        }

        then:
        def gatlingRunSimulations = gatlingExt.resolveSimulations(gatlingRunTask.simulations)
        gatlingRunSimulations == gatlingExt.resolveSimulations()
        and:
        gatlingRunSimulations.size() == 1
        and:
        gatlingRunSimulations.head().endsWith("Advanced1Simulation")
    }

    def "should override simulations filter via own properties"() {
        when:
        project.gatlingRun.simulations = {
            include "**/*Basic1Simulation*"
        }

        then: "task simulations ar enot equals to extension"
        def gatlingRunSimulations = gatlingExt.resolveSimulations(gatlingRunTask.simulations)
        gatlingRunSimulations != gatlingExt.resolveSimulations()
        and:
        gatlingRunSimulations.size() == 1
        and:
        gatlingRunSimulations.head().endsWith("Basic1Simulation")
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

package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GatlingPluginTest extends Specification {
    def "should create custom configurations"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.github.lkishalmi.gatling'

        then:
        ['gatling', 'gatlingCompile', 'gatlingRuntime'].every {
            project.configurations.getByName(it) != null
        }
    }

    def "should add gatling dependencies"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.github.lkishalmi.gatling'
        and:
        project.evaluate()

        then:
        def gatlingExt = project.extensions.findByType(GatlingPluginExtension)
        project.configurations.getByName("gatling").allDependencies.find {
            it.name == "gatling-charts-highcharts" && it.version == gatlingExt.toolVersion
        }
    }

    def "should allow overriding gatling version via extension"() {
        when:
        Project project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'com.github.lkishalmi.gatling'
        and:
        project.gatling {
            toolVersion = '1.1.1'
        }
        and:
        project.evaluate()

        then:
        project.configurations.getByName("gatling").allDependencies.find {
            it.name == "gatling-charts-highcharts" && it.version == "1.1.1"
        }
    }

    def "should store filtered simulations as a task property"() {
        when:
        Project project = ProjectBuilder.builder()
            .withProjectDir(new File("src/test/resources/gradle-layout")).build()
        project.pluginManager.apply 'com.github.lkishalmi.gatling'
        and:
        project.evaluate()

        then:
        Collection<String> simulations = project.tasks["gatling"].simulations
        simulations.size() == 2
        and:
        simulations.any { it.endsWith("Advanced1Simulation") }
        and:
        simulations.any { it.endsWith("Basic1Simulation") }
    }

    def "should allow overriding simulations filter via extension"() {
        when:
        Project project = ProjectBuilder.builder()
            .withProjectDir(new File("src/test/resources/gradle-layout")).build()
        project.pluginManager.apply 'com.github.lkishalmi.gatling'
        and:
        project.gatling {
            simulations = {
                include "**/*Advanced1Simulation*"
            }
        }
        and:
        project.evaluate()

        then:
        Collection<String> simulations = project.tasks["gatling"].simulations
        simulations.size() == 1
        and:
        simulations.every { it.endsWith("Advanced1Simulation") }
    }
}

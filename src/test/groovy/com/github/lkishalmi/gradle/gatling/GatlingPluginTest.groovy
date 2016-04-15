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

    def "should allow to override gatling version via extension"() {
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
}

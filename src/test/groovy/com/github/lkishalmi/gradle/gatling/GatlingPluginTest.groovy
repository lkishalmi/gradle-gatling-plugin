package com.github.lkishalmi.gradle.gatling

class GatlingPluginTest extends GatlingUnitSpec {

    def "should create gatling configurations"() {
        expect:
        ['gatling', 'gatlingCompile', 'gatlingRuntime'].every {
            project.configurations.getByName(it) != null
        }
    }

    def "should add gatling dependencies"() {
        when:
        project.evaluate()
        then:
        def gatlingExt = project.extensions.findByType(GatlingPluginExtension)
        project.configurations.getByName("gatling").allDependencies.find {
            it.name == "gatling-charts-highcharts" && it.version == gatlingExt.toolVersion
        }
    }

    def "should allow overriding gatling version via extension"() {
        when:
        project.gatling { toolVersion = '1.1.1' }
        and:
        project.evaluate()

        then:
        project.configurations.getByName("gatling").allDependencies.find {
            it.name == "gatling-charts-highcharts" && it.version == "1.1.1"
        }
    }

    def "should create gatlingRun task"() {
        expect:
        with(gatlingRunTask) {
            it instanceof GatlingRunTask
            it.simulations == gatlingExt.simulations
            it.jvmArgs == gatlingExt.jvmArgs
        }
    }

    def "should create processGatlingResources task"() {
        expect:
        project.tasks.getByName("processGatlingResources") != null
        and:
        project.tasks.getByName("processGatlingResources").actions.find { it.action instanceof LogbackConfigTaskAction}
    }
}

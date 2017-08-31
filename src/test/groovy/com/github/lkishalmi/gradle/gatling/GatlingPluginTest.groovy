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

    def "should allow overriding scala version via extension"() {
        when:
        project.gatling { scalaVersion = '2.11.3' }
        and:
        project.evaluate()

        then:
        project.configurations.getByName("gatlingCompile").allDependencies.find {
            it.name == "scala-library" && it.version == "2.11.3"
        }
    }

    def "should allow overwriting source root via extension"() {
        when:
        project.gatling { sourceRoot = 'src/test/scala'}
        and:
        project.evaluate()

        then:
        gatlingExt.simulationsDir() == "src/test/scala/simulations"
    }

    def "should allow overwriting simulations dir via extension"() {
        when:
        project.gatling {
            sourceRoot = 'src/test/scala'
            simulationsDir = "user-files/simulations"
        }
        and:
        project.evaluate()

        then:
        gatlingExt.simulationsDir() == "src/test/scala/user-files/simulations"
    }

    def "should allow overwriting data dir via extension"() {
        when:
        project.gatling {
            sourceRoot = 'src/test/scala'
            dataDir = "user-files/data"
        }
        and:
        project.evaluate()

        then:
        gatlingExt.dataDir() == "src/test/scala/user-files/data"
    }

    def "should allow overwriting bodies dir via extension"() {
        when:
        project.gatling {
            sourceRoot = 'src/test/scala'
            dataDir = "user-files/bodies"
        }
        and:
        project.evaluate()

        then:
        gatlingExt.dataDir() == "src/test/scala/user-files/bodies"
    }

    def "should allow overwriting conf dir via extension"() {
        when:
        project.gatling {
            sourceRoot = 'src/test/scala'
            dataDir = "user-files/conf"
        }
        and:
        project.evaluate()

        then:
        gatlingExt.dataDir() == "src/test/scala/user-files/conf"
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

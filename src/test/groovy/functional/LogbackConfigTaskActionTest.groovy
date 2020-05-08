package functional

import helper.GatlingFuncSpec
import org.gradle.testkit.runner.BuildResult

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class LogbackConfigTaskActionTest extends GatlingFuncSpec {

    static def PROCESS_GATLING_RESOURCES_TASK_NAME = "processGatlingResources"

    File logbackConfig

    XmlSlurper xml = new XmlSlurper()

    def setup() {
        prepareTest()
        logbackConfig = new File(buildDir, "resources/gatling/logback.xml".toString())
    }

    def "should create sample logback using logLevel from extension"() {
        when:
        BuildResult result = executeGradle(PROCESS_GATLING_RESOURCES_TASK_NAME)
        then:
        result.task(":$PROCESS_GATLING_RESOURCES_TASK_NAME").outcome == SUCCESS
        and:
        logbackConfig.exists()
        and:
        xml.parse(logbackConfig).root.@level == "WARN"
    }

    def "should override logLevel via extension"() {
        given:
        buildFile << 'gatling { logLevel = "QQQQ" }'
        when:
        BuildResult result = executeGradle(PROCESS_GATLING_RESOURCES_TASK_NAME)
        then:
        result.task(":$PROCESS_GATLING_RESOURCES_TASK_NAME").outcome == SUCCESS
        and:
        logbackConfig.exists()
        and:
        xml.parse(logbackConfig).root.@level == "QQQQ"
    }

    def "should not create sample logback.xml when it exists"() {
        given: ""
        new File(projectDir.root, "src/gatling/resources/logback.xml") << """<fakeLogback attr="value"/>"""
        when:
        BuildResult result = executeGradle(PROCESS_GATLING_RESOURCES_TASK_NAME)
        then:
        result.task(":$PROCESS_GATLING_RESOURCES_TASK_NAME").outcome == SUCCESS
        and:
        logbackConfig.exists()
        and:
        xml.parse(logbackConfig).@attr == "value"
    }
}

package unit

import helper.GatlingUnitSpec
import org.gradle.language.jvm.tasks.ProcessResources

class LogbackConfigTaskActionTest extends GatlingUnitSpec {

    ProcessResources resourcesTask

    File logbackConfig

    XmlSlurper xml = new XmlSlurper()

    def setup() {
        resourcesTask = project.tasks['processGatlingResources']
        logbackConfig = new File(buildDir, "resources/gatling/logback.xml")
    }

    def "should create sample logback using logLevel from extension"() {
        when:
        resourcesTask.execute()
        then:
        logbackConfig.exists()
        and:
        xml.parse(logbackConfig).root.@level == gatlingExt.logLevel
    }

    def "should override logLevel via extension"() {
        when:
        project.gatling { logLevel = "QQQQ" }
        and:
        resourcesTask.execute()
        then:
        logbackConfig.exists()
        and:
        def xml = xml.parse(logbackConfig)
        xml.root.@level == gatlingExt.logLevel
        and:
        xml.root.@level == "QQQQ"
    }

    def "should not create sample logback.xml when it exists"() {
        given: ""
        new File(projectDir.root, "src/gatling/resources/logback.xml") << """<fakeLogback attr="value"/>"""
        when:
        resourcesTask.execute()
        then:
        logbackConfig.exists()
        and:
        xml.parse(logbackConfig).@attr == "value"
    }
}

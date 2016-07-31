package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Task

class LogbackConfigTaskActionTest extends GatlingUnitSpec {

    Task resourcesTask

    File logbackConfig

    def setup() {
        resourcesTask = project.tasks['processGatlingResources']
        logbackConfig = new File(testProjectBuildDir, "resources/gatling/logback.xml")
    }

    def "should create sample logback using logLevel from extension"() {
        when:
        resourcesTask.execute()
        then:
        logbackConfig.exists()
        and:
        new XmlSlurper().parse(logbackConfig).root.@level == gatlingExt.logLevel
    }

    def "should override logLevel via extension"() {
        when:
        project.gatling { logLevel = "QQQQ" }
        and:
        resourcesTask.execute()

        then:
        logbackConfig.exists()
        and:
        def xml = new XmlSlurper().parse(logbackConfig)
        xml.root.@level == gatlingExt.logLevel
        and:
        xml.root.@level == "QQQQ"
    }

    def "should not create sample logback.xml when exist in `conf` folder"() {
        given: ""
        new File(testProjectDir.root, "src/gatling/resources/conf/logback.xml") << """<fakeLogback attr="value"/>"""
        when:
        resourcesTask.execute()

        then:
        logbackConfig.exists()
        and:
        new XmlSlurper().parse(logbackConfig).@attr == "value"
    }

}

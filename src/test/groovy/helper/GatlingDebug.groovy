package helper

import com.github.lkishalmi.gradle.gatling.GatlingPlugin
import groovy.json.JsonSlurper
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

class GatlingDebug {

    Map systemProperties
    Map env

    List<String> jvmArgs
    List<String> argv

    Map heap

    GatlingDebug(BuildResult buildResult) {
        assert buildResult.task(":$GatlingPlugin.GATLING_RUN_TASK_NAME").outcome == TaskOutcome.SUCCESS

        def lines = buildResult.output.readLines().findAll { it.startsWith("@@@@") }
        assert lines.size() == 4

        def jsonSlurper = new JsonSlurper()

        this.heap = jsonSlurper.parseText(lines.find { it.startsWith("@@@@.heap") } - "@@@@.heap ")
        this.jvmArgs = jsonSlurper.parseText(lines.find { it.startsWith("@@@@.jvm") } - "@@@@.jvm ")
        this.systemProperties = jsonSlurper.parseText((lines.find { it.startsWith("@@@@.sys") } - "@@@@.sys "))
        this.env = jsonSlurper.parseText((lines.find { it.startsWith("@@@@.env") } - "@@@@.env "))

        this.argv = this.systemProperties["sun.java.command"].split("\\s") as List
    }
}

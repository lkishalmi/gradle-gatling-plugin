package functional

import com.github.lkishalmi.gradle.gatling.GatlingPluginExtension
import helper.GatlingDebug
import helper.GatlingFuncSpec
import org.gradle.testkit.runner.BuildResult
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.contrib.java.lang.system.RestoreSystemProperties

import static com.github.lkishalmi.gradle.gatling.GatlingPlugin.GATLING_RUN_TASK_NAME

class WhenRunSimulationSysPropsJvmArgsSpec extends GatlingFuncSpec {

    @Rule
    public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties()

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    def setup() {
        prepareTest("/gatling-debug")
        new File(new File(projectDir.root, "src/gatling/resources"), "gatling.conf").text = "gatling.data.writers = []"
    }

    def "should set memory limits from jvmArgs of gatling extension"() {
        when: "default Xmx from gatling extension"
        BuildResult result = executeGradle(GATLING_RUN_TASK_NAME)
        then:
        with(new GatlingDebug(result)) {
            heap.max == 1024 * 1024 * 1024// 1GB
        }

        when: "override via gatling extension"
        buildFile << """
gatling {
    jvmArgs = ['-Xms32m']
}    
"""
        and:
        result = executeGradle("--rerun-tasks", GATLING_RUN_TASK_NAME)
        then:
        with(new GatlingDebug(result)) {
            heap.min == 32 * 1024 * 1024
        }
    }

    def "should configure jvmArgs from extension"() {
        when: "default jvmArgs from gatling extension"
        BuildResult result = executeGradle(GATLING_RUN_TASK_NAME)
        then:
        with(new GatlingDebug(result)) {
            jvmArgs.sort() == GatlingPluginExtension.DEFAULT_JVM_ARGS.findAll { it.startsWith("-X") }.sort()
        }

        when: "override via gatling extension"
        buildFile << """
gatling {
    jvmArgs = ['-Xms32m', '-XX:+UseG1GC']
}
"""
        and:
        result = executeGradle("--rerun-tasks", GATLING_RUN_TASK_NAME)
        then:
        with(new GatlingDebug(result)) {
            jvmArgs.sort() == ['-Xms32m', '-XX:+UseG1GC'].sort()
        }
    }

    def "should override jvmArgs from gatlingRun properties"() {
        when: "override via gatlingRun"
        buildFile << """
gatling {
    jvmArgs = ['-Xms32m', '-XX:+AggressiveOpts']
}
gatlingRun {
    jvmArgs = ['-Xms128m', '-XX:+UseG1GC']
}
"""
        and:
        def result = executeGradle(GATLING_RUN_TASK_NAME)
        then:
        with(new GatlingDebug(result)) {
            jvmArgs.sort() == ['-Xms128m', '-XX:+UseG1GC'].sort()
        }
    }

    def "should configure system properties from extension"() {
        when: "defalts from extension"
        def result = executeGradle(GATLING_RUN_TASK_NAME)
        then:
        with(new GatlingDebug(result)) {
            systemProperties.keySet().intersect(GatlingPluginExtension.DEFAULT_SYSTEM_PROPS.keySet()).size() == GatlingPluginExtension.DEFAULT_SYSTEM_PROPS.size()
        }

        when: "override via extension"
        buildFile << """
gatling {
    systemProperties = ['gradle_gatling_1': 'aaa', 'gradle_gatling_2' : 'bbb']
}
"""
        and:
        result = executeGradle("--rerun-tasks", GATLING_RUN_TASK_NAME)
        then:
        with(new GatlingDebug(result)) {
            systemProperties.keySet().findAll { it.startsWith("gradle_gatling_") } == ['gradle_gatling_1', 'gradle_gatling_2'] as Set
            systemProperties["gradle_gatling_1"] == "aaa"
            systemProperties["gradle_gatling_2"] == "bbb"

            systemProperties.keySet().intersect(GatlingPluginExtension.DEFAULT_SYSTEM_PROPS.keySet()).size() == 0
        }
    }

    def "should extend system properties from extension"() {
    }

    def "should extend system properties from gatlingRun properties"() {
        given: "override via gatlingRun"
        buildFile << """
gatling {
    systemProperties = ['gradle_gatling_1': 'aaa', 'gradle_gatling_2' : 'bbb']
}
gatlingRun {
    systemProperties = ['gradle_gatling_2' : 'qwerty', 'gradle_gatling_3' : 'ccc']    
}
"""
        when:
        def result = executeGradle(GATLING_RUN_TASK_NAME)
        then:
        with(new GatlingDebug(result)) {
            systemProperties.keySet().findAll { it.startsWith("gradle_gatling_") } == ['gradle_gatling_2', 'gradle_gatling_3'] as Set
            systemProperties["gradle_gatling_2"] == "qwerty"
        }
    }

    def "should pass env vars upstream"() {
        given:
        environmentVariables.set("GRADLE_GATLING_ENV_UPSTREAM", "env_upstream_value")
        when:
        def result = executeGradle(GATLING_RUN_TASK_NAME)
        then:
        with(new GatlingDebug(result)) {
            env["GRADLE_GATLING_ENV_UPSTREAM"] == "env_upstream_value"
        }
    }

    def "should pass system properties upstream"() {
        given:
        System.setProperty("gradle_gatling_sys_upstream", "sys_upstream_value")
        when:
        def result = executeGradle(GATLING_RUN_TASK_NAME)
        then:
        with(new GatlingDebug(result)) {
            systemProperties["gradle_gatling_sys_upstream"] == "sys_upstream_value"
        }
    }
}

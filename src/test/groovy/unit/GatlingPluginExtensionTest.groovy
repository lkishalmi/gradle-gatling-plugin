package unit

import helper.GatlingUnitSpec

import static com.github.lkishalmi.gradle.gatling.GatlingPluginExtension.DEFAULT_JVM_ARGS
import static com.github.lkishalmi.gradle.gatling.GatlingPluginExtension.DEFAULT_SYSTEM_PROPS

class GatlingPluginExtensionTest extends GatlingUnitSpec {

    def "should override jvm args via property"() {
        given:
        assert gatlingExt.jvmArgs == DEFAULT_JVM_ARGS
        when:
        gatlingExt.jvmArgs = ["qwe"].toSet()
        then:
        gatlingExt.jvmArgs == ["qwe"].toSet()
        when:
        gatlingExt.jvmArgs = ["asd"].toSet()
        then:
        gatlingExt.jvmArgs == ["asd"].toSet()
    }

    def "should extend jvm args via method with list"() {
        given:
        assert gatlingExt.jvmArgs == DEFAULT_JVM_ARGS
        when:
        gatlingExt.jvmArgs "qwe", "asd"
        gatlingExt.jvmArgs "qwe", "asd"
        then:
        gatlingExt.jvmArgs.size() == DEFAULT_JVM_ARGS.size() + 2
        gatlingExt.jvmArgs.containsAll(DEFAULT_JVM_ARGS)
        gatlingExt.jvmArgs - DEFAULT_JVM_ARGS == ["asd", "qwe"].toSet()
        when:
        gatlingExt.jvmArgs "111", "222"
        then:
        gatlingExt.jvmArgs.containsAll(DEFAULT_JVM_ARGS)
        gatlingExt.jvmArgs - DEFAULT_JVM_ARGS == ["111", "222", "asd", "qwe"].toSet()
    }

    def "should override systemProperties via property"() {
        given:
        assert gatlingExt.systemProperties == DEFAULT_SYSTEM_PROPS
        when:
        gatlingExt.systemProperties = ["qwe_key": "qwe_value"]
        then:
        gatlingExt.systemProperties == ["qwe_key": "qwe_value"]
        when:
        gatlingExt.systemProperties = ["asd_key": "asd_value"]
        then:
        gatlingExt.systemProperties == ["asd_key": "asd_value"]
    }

    def "should extend systemProperties via method with map"() {
        given:
        assert gatlingExt.systemProperties == DEFAULT_SYSTEM_PROPS
        when:
        gatlingExt.systemProperties(["qwe": "asd"])
        gatlingExt.systemProperties(["qwe": "asd"])
        then:
        gatlingExt.systemProperties.size() == DEFAULT_SYSTEM_PROPS.size() + 1
        gatlingExt.systemProperties.intersect(DEFAULT_SYSTEM_PROPS) == DEFAULT_SYSTEM_PROPS
        gatlingExt.systemProperties - DEFAULT_SYSTEM_PROPS == ["qwe": "asd"]
        when:
        gatlingExt.systemProperties(["111": "222"])
        then:
        gatlingExt.systemProperties.size() == DEFAULT_SYSTEM_PROPS.size() + 2
        gatlingExt.systemProperties.intersect(DEFAULT_SYSTEM_PROPS) == DEFAULT_SYSTEM_PROPS
        gatlingExt.systemProperties - DEFAULT_SYSTEM_PROPS == ["111": "222", "qwe": "asd"]
    }

    def "should override environment via property"() {
        given:
        assert gatlingExt.environment == System.getenv()
        when:
        gatlingExt.environment = ["qwe_key": "qwe_value"]
        then:
        gatlingExt.environment == ["qwe_key": "qwe_value"]
        when:
        gatlingExt.environment = ["asd_key": "asd_value"]
        then:
        gatlingExt.environment == ["asd_key": "asd_value"]
    }

    def "should extend environment via method with map"() {
        given:
        def initialEnv = System.getenv()
        assert gatlingExt.environment == initialEnv
        when:
        gatlingExt.environment(["qwe": "asd"])
        gatlingExt.environment(["qwe": "asd"])
        then:
        gatlingExt.environment.size() == initialEnv.size() + 1
        gatlingExt.environment.intersect(initialEnv) == initialEnv
        gatlingExt.environment - initialEnv == ["qwe": "asd"]
        when:
        gatlingExt.environment(["111": "222"])
        then:
        gatlingExt.environment.size() == initialEnv.size() + 2
        gatlingExt.environment.intersect(initialEnv) == initialEnv
        gatlingExt.environment - initialEnv == ["111": "222", "qwe": "asd"]
    }
}

package com.github.lkishalmi.gradle.gatling

trait JvmConfigurable {

    private Set<String> jvmArgs

    private Map<String, Object> systemProperties

    private Map<String, String> environment = System.getenv()

    Set<String> getJvmArgs() {
        return jvmArgs
    }

    void setJvmArgs(Set<String> jvmArgs) {
        this.jvmArgs = jvmArgs
    }

    void jvmArgs(String... jvmArgs) {
        def v = new HashSet<String>(this.jvmArgs)
        v.addAll(jvmArgs)
        setJvmArgs(v)
    }

    Map<String, Object> getSystemProperties() {
        return systemProperties
    }

    void setSystemProperties(Map<String, Object> systemProperties) {
        this.systemProperties = systemProperties
    }

    void systemProperties(Map<String, Object> sysProps) {
        Map<String, Object> m = new HashMap(getSystemProperties())
        m.putAll(sysProps)
        setSystemProperties(m)
    }

    Map<String, String> getEnvironment() {
        return environment
    }

    void setEnvironment(Map<String, String> environment) {
        this.environment = environment
    }

    void environment(Map<String, Object> env) {
        Map<String, Object> m = new HashMap(getEnvironment())
        m.putAll(env)
        setEnvironment(m)
    }
}

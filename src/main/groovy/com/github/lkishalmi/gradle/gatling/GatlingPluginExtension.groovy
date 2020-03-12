package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Project

class GatlingPluginExtension implements JvmConfigurable {

    static final String GATLING_MAIN_CLASS = 'io.gatling.app.Gatling'

    static final String SIMULATIONS_DIR = "src/gatling/simulations"

    static final String RESOURCES_DIR = "src/gatling/resources"

    static final String GATLING_TOOL_VERSION = '3.3.1'

    static final String SCALA_VERSION = '2.12.8'

    static final Closure DEFAULT_SIMULATIONS = { include("**/*Simulation*.scala") }

    def toolVersion = GATLING_TOOL_VERSION

    def scalaVersion = SCALA_VERSION

    Closure simulations = DEFAULT_SIMULATIONS

    def includeMainOutput = true
    def includeTestOutput = true

    String logLevel = "WARN"

    private final Project project

    GatlingPluginExtension(Project project) {
        this.project = project
        this.jvmArgs = DEFAULT_JVM_ARGS
        this.systemProperties = DEFAULT_SYSTEM_PROPS
    }
}

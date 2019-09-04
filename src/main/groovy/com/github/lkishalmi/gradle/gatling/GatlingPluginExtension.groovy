package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Project

import java.nio.file.Path
import java.nio.file.Paths

class GatlingPluginExtension {

    static final String GATLING_MAIN_CLASS = 'io.gatling.app.Gatling'

    static final String SIMULATIONS_DIR = "src/gatling/simulations"

    static final String RESOURCES_DIR = "src/gatling/resources"

    static final String GATLING_TOOL_VERSION = '3.2.1'

    static final String SCALA_VERSION = '2.12.8'

    static final List<String> DEFAULT_JVM_ARGS = [
        '-server',
        '-Xmx1G',
        '-XX:+HeapDumpOnOutOfMemoryError',
        '-XX:+UseG1GC',
        '-XX:MaxGCPauseMillis=30',
        '-XX:G1HeapRegionSize=16m',
        '-XX:InitiatingHeapOccupancyPercent=75',
        '-XX:+ParallelRefProcEnabled',
        '-XX:+PerfDisableSharedMem',
        '-XX:+AggressiveOpts',
        '-XX:+OptimizeStringConcat'
    ]

    static final Map DEFAULT_SYSTEM_PROPS = ["java.net.preferIPv4Stack": true, "java.net.preferIPv6Addresses": false]

    static final Closure DEFAULT_SIMULATIONS = {
        include "**/*Simulation*.scala"
    }

    def toolVersion = GATLING_TOOL_VERSION

    def scalaVersion = SCALA_VERSION

    def jvmArgs = DEFAULT_JVM_ARGS

    def systemProperties = DEFAULT_SYSTEM_PROPS

    def simulations = DEFAULT_SIMULATIONS

    def includeMainOutput = true
    def includeTestOutput = true

    String logLevel = "WARN"

    private final Project project

    GatlingPluginExtension(Project project) {
        this.project = project
    }

    Iterable<String> resolveSimulations(Closure simulationFilter) {
        def scalaDirs = project.sourceSets.gatling.scala.srcDirs.collect { Paths.get(it.absolutePath) }
        def scalaFiles = project.sourceSets.gatling.scala.matching(simulationFilter).collect { Paths.get(it.absolutePath) }

        scalaFiles.collect { Path simu ->
            scalaDirs.find { simu.startsWith(it) }.relativize(simu).join(".") - ".scala"
        }
    }
}

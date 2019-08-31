package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Project

import java.nio.file.Paths

class GatlingPluginExtension {

    static final String SIMULATIONS_DIR = "src/gatling/simulations"

    static final String RESOURCES_DIR = "src/gatling/resources"

    def toolVersion = '3.2.1'

    def scalaVersion = '2.12.8'

    def jvmArgs = [
            '-server',
            '-Xmx1G',
            '-XX:+UseG1GC',
            '-XX:MaxGCPauseMillis=30',
            '-XX:G1HeapRegionSize=16m',
            '-XX:InitiatingHeapOccupancyPercent=75',
            '-XX:+ParallelRefProcEnabled',
            '-XX:+PerfDisableSharedMem',
            '-XX:+AggressiveOpts',
            '-XX:+OptimizeStringConcat',
            '-XX:+HeapDumpOnOutOfMemoryError',
            '-Djava.net.preferIPv4Stack=true',
            '-Djava.net.preferIPv6Addresses=false'
    ]

    def simulations = {
        include "**/*Simulation*.scala"
    }

    def includeMainOutput = true
    def includeTestOutput = true

    String logLevel = "WARN"

    private final Project project

    GatlingPluginExtension(Project project) {
        this.project = project
    }

    Iterable<String> resolveSimulations(Closure simulationFilter = getSimulations()) {
        def p = this.project
        project.sourceSets.gatling.allScala.matching(simulationFilter).collect { File simu ->
            Paths.get(p.file(SIMULATIONS_DIR).toURI()).relativize(Paths.get(simu.toURI())).join(".") - ".scala"
        }
    }
}

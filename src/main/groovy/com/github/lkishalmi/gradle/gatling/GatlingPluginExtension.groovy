package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Project

import java.nio.file.Paths

class GatlingPluginExtension {

    def toolVersion = '2.3.1'
    def scalaVersion = '2.12.3'

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
        include "**/*Simulation.scala"
    }

    def sourceRoot = ''
    def simulationsDir = 'simulations'
    def dataDir = 'data'
    def bodiesDir = 'bodies'
    def confDir = 'conf'

    def includeMainOutput = true
    def includeTestOutput = true

    String logLevel = "WARN"

    private final boolean isGatlingLayout

    private final Project project

    GatlingPluginExtension(Project project) {
        this.project = project
        this.isGatlingLayout = project.file("src/gatling/simulations").exists()
    }

    String simulationsDir() {
        if (isAutoDetect()) {
            "src/gatling/${isGatlingLayout ? "simulations" : "scala"}"
        } else {
            "${sourceRoot}/${simulationsDir}"
        }
    }

    String dataDir() {
        if (isAutoDetect()) {
            "src/gatling${isGatlingLayout ? "" : "/resources"}/data"
        } else {
            "${sourceRoot}/${dataDir}"
        }

    }

    String bodiesDir() {
        if (isAutoDetect()) {
            "src/gatling${isGatlingLayout ? "" : "/resources"}/bodies"
        } else {
            "${sourceRoot}/${bodiesDir}"
        }

    }

    String confDir() {
        if (isAutoDetect()) {
            "src/gatling${isGatlingLayout ? "" : "/resources"}/conf"
        } else {
            "${sourceRoot}/${confDir}"
        }
    }

    String scalaVersion() {

        "${scalaVersion}"
    }

    boolean isAutoDetect() {
        sourceRoot == null || sourceRoot.isEmpty()
    }

    Iterable<String> resolveSimulations(Closure simulationFilter = getSimulations()) {
        def p = this.project
        project.sourceSets.gatling.allScala.matching(simulationFilter).collect { File simu ->
            Paths.get(p.file(this.simulationsDir()).toURI()).relativize(Paths.get(simu.toURI())).join(".") - ".scala"
        }
    }
}

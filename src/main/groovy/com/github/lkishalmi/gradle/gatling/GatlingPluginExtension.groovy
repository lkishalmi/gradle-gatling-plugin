package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Project

import java.nio.file.Paths

class GatlingPluginExtension {

    def toolVersion = '2.3.0'
    def scalaVersion = '2.12.3'

    def jvmArgs = [
        '-server',
        '-XX:+UseThreadPriorities',
        '-XX:ThreadPriorityPolicy=42',
        '-Xms512M',
        '-Xmx512M',
        '-Xmn100M',
        '-XX:+HeapDumpOnOutOfMemoryError',
        '-XX:+AggressiveOpts',
        '-XX:+OptimizeStringConcat',
        '-XX:+UseFastAccessorMethods',
        '-XX:+UseParNewGC',
        '-XX:+UseConcMarkSweepGC',
        '-XX:+CMSParallelRemarkEnabled',
        '-Djava.net.preferIPv4Stack=true',
        '-Djava.net.preferIPv6Addresses=false'
    ]

    def simulations = {
        include "**/*Simulation.scala"
    }

    def sourceRoot = ''
    def simulationsDir = 'simulations'
    def confDir = 'conf'
    def dataDir = 'data'
    def bodiesDir = 'bodies'

    String logLevel = "WARN"

    private final boolean isGatlingLayout
    private final boolean isAutoDetect

    private final Project project

    GatlingPluginExtension(Project project) {
        this.project = project
        this.isGatlingLayout = project.file("src/gatling/simulations").exists()
        this.isAutoDetect = sourceRoot.isEmpty()

    }

    String simulationsDir() {
        if (isAutoDetect) {
            "src/gatling/${isGatlingLayout ? "simulations" : "scala"}"
        } else {
            "${sourceRoot}/${simulationsDir}"
        }

    }

    String dataDir() {
        if (isAutoDetect) {
            "src/gatling${isGatlingLayout ? "" : "/resources"}/data"
        } else {
            "${sourceRoot}/${dataDir}"
        }

    }

    String bodiesDir() {
        if (isAutoDetect) {
            "src/gatling${isGatlingLayout ? "" : "/resources"}/bodies"
        } else {
            "${sourceRoot}/${bodiesDir}"
        }

    }

    String confDir() {
        if (isAutoDetect) {
            "src/gatling${isGatlingLayout ? "" : "/resources"}/conf"
        } else {
            "${sourceRoot}/${confDir}"
        }
    }

    Iterable<String> resolveSimulations(Closure simulationFilter = getSimulations()) {
        def p = this.project
        project.sourceSets.gatling.allScala.matching(simulationFilter).collect { File simu ->
            Paths.get(p.file(this.simulationsDir()).toURI()).relativize(Paths.get(simu.toURI())).join(".") - ".scala"
        }
    }
}

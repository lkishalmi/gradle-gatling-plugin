package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Project

import java.nio.file.Paths

class GatlingPluginExtension {

    def toolVersion = '2.2.2'

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

    File resultFolder

    String logLevel = "WARN"

    private final boolean isGatlingLayout

    private final Project project

    GatlingPluginExtension(Project project) {
        this.project = project
        this.isGatlingLayout = project.file("src/gatling/simulations").exists()
    }

    String simulationsDir() {
        "src/gatling/${isGatlingLayout ? "simulations" : "scala"}"
    }

    String dataDir() {
        "src/gatling${isGatlingLayout ? "" : "/resources"}/data"
    }

    String bodiesDir() {
        "src/gatling${isGatlingLayout ? "" : "/resources"}/bodies"
    }

    String confDir() {
        "src/gatling${isGatlingLayout ? "" : "/resources"}/conf"
    }

    Iterable<String> resolveSimulations(Closure simulationFilter = getSimulations()) {
        def p = this.project
        project.sourceSets.gatling.allScala.matching(simulationFilter).collect { File simu ->
            Paths.get(p.file(this.simulationsDir()).toURI()).relativize(Paths.get(simu.toURI())).join(".") - ".scala"
        }
    }
}

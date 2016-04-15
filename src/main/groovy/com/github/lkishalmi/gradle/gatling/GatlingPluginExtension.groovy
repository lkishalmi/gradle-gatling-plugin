package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Project

/**
 *
 * @author Laszlo Kishalmi
 */
class GatlingPluginExtension {

    def toolVersion = '2.2.0'

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

    private final boolean isGatlingLayout

    GatlingPluginExtension(Project project) {
        this.isGatlingLayout = project.file("src/gatling/simulations").exists() && project.file("src/gatling/data").exists() && project.file("src/gatling/bodies").exists()
    }

    String simulationsDir() {
        "src/gatling/${ isGatlingLayout ? "simulations" : "scala" }"
    }

    String dataDir() {
        "src/gatling${ isGatlingLayout ? "" : "/resources" }/data"
    }

    String bodiesDir() {
        "src/gatling${ isGatlingLayout ? "" : "/resources" }/bodies"
    }

    String confDir() {
        "src/gatling${ isGatlingLayout ? "" : "/resources" }/conf"
    }

}

package com.github.lkishalmi.gradle.gatling

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.util.GradleVersion

import java.nio.file.Path
import java.nio.file.Paths

class GatlingRunTask extends DefaultTask {

    def jvmArgs

    def systemProperties

    def simulations

    List<String> createGatlingArgs() {
        def retval = []

        if (GradleVersion.current() >= GradleVersion.version('4.0')) {
            File scalaClasses = project.sourceSets.gatling.output.classesDirs.filter {
                it.parentFile.name == 'scala'
            }.singleFile

            retval += ['-bf', scalaClasses.absolutePath]
        } else {
            retval += ["-bf", "${project.sourceSets.gatling.output.classesDir}"]
        }

        retval += ["-rsf", "${project.sourceSets.gatling.output.resourcesDir}"]
        retval += ["-rf", "${project.reportsDir}/gatling"]

        retval
    }

    Iterable<String> resolveSimulations() {
        Iterable<String> retval

        def simulationFilter = this.simulations ?: project.gatling.simulations

        if (simulationFilter != null && simulationFilter instanceof Closure<Iterable<String>>) {
            def scalaDirs = project.sourceSets.gatling.scala.srcDirs.collect { Paths.get(it.absolutePath) }
            def scalaFiles = project.sourceSets.gatling.scala.matching(simulationFilter).collect { Paths.get(it.absolutePath) }

            retval = scalaFiles.collect { Path simu ->
                scalaDirs.find { simu.startsWith(it) }.relativize(simu).join(".") - ".scala"
            }
        } else if (simulationFilter != null && simulationFilter instanceof Iterable<String>) {
            def scalaDirs = project.sourceSets.gatling.scala.srcDirs
            retval = simulationFilter.findAll { simuClz ->
                def file = simuClz.replaceAll("\\.", "/")
                scalaDirs.any { new File(it, "${file}.scala").exists() }
            }
        } else {
            throw new IllegalArgumentException("`simulations` property neither Closure nor Iterable<String>, simulations: $simulationFilter")
        }

        retval
    }

    @TaskAction
    void gatlingRun() {
        def self = this

        def failures = [:]

        resolveSimulations().each { String simuName ->
            try {
                project.javaexec {
                    main = GatlingPluginExtension.GATLING_MAIN_CLASS
                    classpath = project.configurations.gatlingRuntime

                    jvmArgs self.jvmArgs ?: project.gatling.jvmArgs

                    args self.createGatlingArgs()
                    args "-s", simuName

                    systemProperties System.properties
                    systemProperties self.systemProperties ?: project.gatling.systemProperties

                    standardInput = System.in
                }
            } catch (Exception e) {
                failures << [(simuName): e]
                getLogger().error("Error executing $simuName", e)
            }
        }

        if (failures.size() > 0) {
            throw new TaskExecutionException(this, new RuntimeException("Some simulations failed : ${failures.keySet().join(", ")}"))
        }
    }
}

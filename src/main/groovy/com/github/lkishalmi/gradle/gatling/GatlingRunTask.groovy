package com.github.lkishalmi.gradle.gatling

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.util.GradleVersion

import java.nio.file.Path
import java.nio.file.Paths

class GatlingRunTask extends DefaultTask {

    def jvmArgs

    def systemProperties

    Closure simulations

    @OutputDirectory
    File gatlingReportDir = project.file("${project.reportsDir}/gatling")

    @InputFiles
    FileTree getSimulationSources() {
        def simulationFilter = this.simulations ?: project.gatling.simulations
        return project.sourceSets.gatling.scala.matching(simulationFilter)
    }

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
        retval += ["-rf", gatlingReportDir.absolutePath]

        retval
    }

    Iterable<String> simulationFilesToFQN() {

        def scalaSrcDirs = project.sourceSets.gatling.scala.srcDirs.collect { Paths.get(it.absolutePath) }
        def scalaFiles = getSimulationSources().collect { Paths.get(it.absolutePath) }

        return scalaFiles.collect { Path srcFile ->
            scalaSrcDirs.find { srcFile.startsWith(it) }.relativize(srcFile).join(".") - ".scala"
        }
    }

    @TaskAction
    void gatlingRun() {
        def self = this

        def failures = [:]

        simulationFilesToFQN().each { String simuName ->
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

package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.process.ExecResult
import org.gradle.process.JavaExecSpec

import java.nio.file.Path
import java.nio.file.Paths

class GatlingRunTask extends DefaultTask implements JvmConfigurable {

    Closure simulations

    @OutputDirectory
    File gatlingReportDir = project.file("${project.reportsDir}/gatling")

    @InputFiles
    FileTree getSimulationSources() {
        def simulationFilter = this.simulations ?: project.gatling.simulations
        return project.sourceSets.gatling.scala.matching(simulationFilter)
    }

    List<String> createGatlingArgs() {

        File scalaClasses = project.sourceSets.gatling.output.classesDirs.filter {
            it.parentFile.name == 'scala'
        }.singleFile

        return ['-bf', scalaClasses.absolutePath,
         "-rsf", "${project.sourceSets.gatling.output.resourcesDir}",
         "-rf", gatlingReportDir.absolutePath]
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
        def gatlingExt = project.extensions.getByType(GatlingPluginExtension)

        Map<String, ExecResult> results = simulationFilesToFQN().collectEntries { String simulationClzName ->
            [(simulationClzName): project.javaexec({ JavaExecSpec exec ->
                exec.main = GatlingPluginExtension.GATLING_MAIN_CLASS
                exec.classpath = project.configurations.gatlingRuntimeClasspath

                exec.jvmArgs this.jvmArgs ?: gatlingExt.jvmArgs
                exec.systemProperties System.properties
                exec.systemProperties this.systemProperties ?: gatlingExt.systemProperties

                exec.args this.createGatlingArgs()
                exec.args "-s", simulationClzName

                exec.standardInput = System.in

                exec.ignoreExitValue = true
            } as Action<JavaExecSpec>)]
        }

        if (results.findAll { it.value.exitValue != 0 }.size() > 0) {
            throw new TaskExecutionException(this, new RuntimeException("There're failed simulations: ${results.keySet().join(", ")}"))
        }

    }
}

package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import org.gradle.api.plugins.scala.ScalaBasePlugin
import org.gradle.api.tasks.scala.ScalaCompile

/**
 *
 * @author Laszlo Kishalmi
 */
class GatlingPlugin implements Plugin<Project> {
    
    private final String GATLING_TASK_NAME = 'gatling'
    private final String GATLING_MAIN_CLASS = 'io.gatling.app.Gatling'
    private Project project
    
    void apply(Project project) {
        this.project = project
        
        project.pluginManager.apply ScalaBasePlugin
        
        def gatling = project.extensions.create('gatling', GatlingExtension)
        
        gatling.simulationsDir = "$project.projectDir/src/gatling/simulations" as File
        gatling.dataDir = "$project.projectDir/src/gatling/data" as File
        gatling.bodiesDir = "$project.projectDir/src/gatling/bodies" as File
        gatling.reportsDir = "$project.buildDir/reports/gatling/" as File
        gatling.confDir = "$project.projectDir/src/gatling/conf" as File
        
        createConfiguration()
        configureGatlingCompile(gatling)
        
        def gatlingTask = project.tasks.create(GATLING_TASK_NAME, Gatling)
        gatlingTask.description = "Executes all Gatling scenarioes"
        gatlingTask.group = "Test"
        
        
        project.tasks.addRule('Pattern: gatling<SimulationName>: Executes a named Gatling simulation.') {def taskName ->
            if (taskName.startsWith(GATLING_TASK_NAME) && !taskName.equals(GATLING_TASK_NAME)) {
                def simulationName = taskName - GATLING_TASK_NAME
                project.tasks.create(taskName, Gatling) {
                    simulation = simulationName
                }
            }
        }
        
        project.tasks.withType(Gatling) { Gatling task ->
            task.dependsOn(project.gatlingCompile)
            configureGatlingTask(task, gatling)
        }
    }

    protected void createConfiguration() {
        project.configurations.create('gatling').with {
            visible = false
            transitive = true
        }
    }
    
    def configureGatlingCompile(GatlingExtension gatling) {
        def config = project.configurations['gatling']
        config.defaultDependencies { dependencies ->
            dependencies.add(this.project.dependencies.create("io.gatling.highcharts:gatling-charts-highcharts:${gatling.toolVersion}"))
        }
        def scalaCompile = project.tasks.create('gatlingCompile', ScalaCompile)
        scalaCompile.conventionMapping.with {
            description = { "Compiles Gatling simulations." }
            source = { project.fileTree(dir: gatling.simulationsDir, includes:['**/*.scala']) }
            classpath = { config }
            destinationDir = { project.file("${project.buildDir}/classes/gatling") }
        }
        project.gradle.projectsEvaluated {
            scalaCompile.scalaCompileOptions.incrementalOptions.with {
                if (!analysisFile) {
                    analysisFile = new File("$project.buildDir/tmp/scala/compilerAnalysis/${scalaCompile.name}.analysis")
                }    
            }
        }
    }
    
    def configureGatlingTask(Gatling task, GatlingExtension gatling) {
        task.conventionMapping.with {
            simulationsDir = { gatling.simulationsDir }
            dataDir = { gatling.dataDir }
            bodiesDir = { gatling.bodiesDir }
            reportsDir = { gatling.reportsDir }
            confDir = { gatling.confDir }
            classesDir = { project.gatlingCompile.destinationDir }
            classpath = { project.configurations['gatling'] + project.files(project.gatlingCompile.destinationDir) }
            mute = { gatling.mute }
        }
    }
}


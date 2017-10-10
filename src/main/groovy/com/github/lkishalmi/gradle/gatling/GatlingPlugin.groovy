package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.ConventionTask
import org.gradle.api.plugins.scala.ScalaPlugin

/**
 *
 * @author Laszlo Kishalmi
 */
class GatlingPlugin implements Plugin<Project> {

    public static def GATLING_EXTENSION_NAME = 'gatling'

    public static def GATLING_RUN_TASK_NAME = 'gatlingRun'

    static String GATLING_TASK_NAME_PREFIX = "$GATLING_RUN_TASK_NAME-"

    private Project project

    void apply(Project project) {
        this.project = project

        project.pluginManager.apply ScalaPlugin

        GatlingPluginExtension gatlingExt = project.extensions.create(GATLING_EXTENSION_NAME, GatlingPluginExtension, project)

        createConfiguration(gatlingExt)

        createGatlingTask(GATLING_RUN_TASK_NAME, gatlingExt)

        project.tasks.getByName("processGatlingResources").doLast(new LogbackConfigTaskAction())

        project.tasks.addRule("Pattern: $GATLING_RUN_TASK_NAME-<SimulationClass>: Executes single Gatling simulation.") {
            String taskName ->
                if (taskName.startsWith(GATLING_TASK_NAME_PREFIX)) {
                    createGatlingTask(taskName, gatlingExt, [(taskName - GATLING_TASK_NAME_PREFIX)])
                }
        }
    }

    void createGatlingTask(String taskName, GatlingPluginExtension gatlingExt, Iterable<String> predefinedSimulations = null) {
        def task = project.tasks.create(name: taskName,
                dependsOn: project.tasks.gatlingClasses, type: GatlingRunTask,
                description: "Execute Gatling simulation", group: "Gatling",
        ) as ConventionTask

        task.convention.plugins["gatling"] = gatlingExt
        task.conventionMapping["jvmArgs"] = { gatlingExt.jvmArgs }

        if (predefinedSimulations) {
            task.configure { simulations = predefinedSimulations }
        } else {
            task.conventionMapping["simulations"] = { gatlingExt.simulations }
        }
    }

    void createConfiguration(GatlingPluginExtension gatlingExt) {
        project.configurations {
            ['gatling', 'gatlingCompile', 'gatlingRuntime'].each() { confName ->
                create(confName) {
                    visible = false
                }
            }
            gatlingCompile.extendsFrom(gatling)
        }

        project.sourceSets {
            gatling {
                scala.srcDirs       = [gatlingExt.simulationsDir()]
                resources.srcDirs   = [gatlingExt.dataDir(),
                                       gatlingExt.bodiesDir(),
                                       gatlingExt.confDir()]
            }
        }

        project.dependencies {


            gatlingCompile project.sourceSets.main.output
            gatlingCompile project.sourceSets.test.output

            gatlingRuntime project.sourceSets.gatling.output
            gatlingRuntime project.sourceSets.gatling.output
        }

        project.afterEvaluate { Project p ->
            p.dependencies {
                gatlingCompile "org.scala-lang:scala-library:${p.extensions.getByType(GatlingPluginExtension).scalaVersion()}"
                gatling "io.gatling.highcharts:gatling-charts-highcharts:${p.extensions.getByType(GatlingPluginExtension).toolVersion}"
            }
        }
    }
}


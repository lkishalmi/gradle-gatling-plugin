package com.github.lkishalmi.gradle.gatling

import org.gradle.util.GradleVersion
import org.gradle.util.VersionNumber

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
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

        if (VersionNumber.parse(GradleVersion.current().version).major < 3) {
            throw new ProjectConfigurationException("This version of plugin doesn't support ${}", null)
        }

        project.pluginManager.apply ScalaPlugin

        GatlingPluginExtension gatlingExt = project.extensions.create(GATLING_EXTENSION_NAME, GatlingPluginExtension, project)

        createConfiguration(gatlingExt)

        createGatlingTask(GATLING_RUN_TASK_NAME, null)

        project.tasks.getByName("processGatlingResources").doLast(new LogbackConfigTaskAction())

        project.tasks.addRule("Pattern: $GATLING_RUN_TASK_NAME-<SimulationClass>: Executes single Gatling simulation.") {
            String taskName ->
                if (taskName.startsWith(GATLING_TASK_NAME_PREFIX)) {
                    createGatlingTask(taskName, (taskName - GATLING_TASK_NAME_PREFIX))
                }
        }
    }

    void createGatlingTask(String taskName, String simulationFQN = null) {
        def task = project.tasks.create(name: taskName,
            dependsOn: project.tasks.gatlingClasses, type: GatlingRunTask,
            description: "Execute Gatling simulation", group: "Gatling")

        if (simulationFQN) {
            task.configure {
                simulations = {
                    include "${simulationFQN.replace('.', '/')}.scala"
                }
            }
        }
    }

    void createConfiguration(GatlingPluginExtension gatlingExt) {
        project.sourceSets {
            gatling {
                scala.srcDirs = [gatlingExt.SIMULATIONS_DIR]
                resources.srcDirs = [gatlingExt.RESOURCES_DIR]
            }
        }

        project.configurations {
            gatling { visible = false }
            gatlingImplementation.extendsFrom(gatling)
        }

        project.dependencies {
            if (gatlingExt.includeMainOutput) {
                gatlingImplementation project.sourceSets.main.output
            }
            if (gatlingExt.includeTestOutput) {
                gatlingImplementation project.sourceSets.test.output
            }

            gatlingRuntimeOnly project.sourceSets.gatling.output
            gatlingRuntimeOnly project.sourceSets.gatling.output
        }

        project.afterEvaluate { Project p ->
            if (p.gatling.toolVersion != null) {
                VersionNumber ver = VersionNumber.parse(p.gatling.toolVersion.toString())
                if (ver.major < 3) {
                    def msg = "Due to breaking changes in Gatling 3.x this plugin does not support: ${p.gatling.toolVersion}\n"
                    msg += "Please try to use plugin version: '0.7.+' for Gatling 2.x or '0.3.+' for Gatling 1.x support."
                    throw new ProjectConfigurationException(msg, null)
                }
            }
            p.dependencies {
                gatlingImplementation "org.scala-lang:scala-library:${p.extensions.getByType(GatlingPluginExtension).scalaVersion}"
                gatling "io.gatling.highcharts:gatling-charts-highcharts:${p.extensions.getByType(GatlingPluginExtension).toolVersion}"
            }
        }
    }
}


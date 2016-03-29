package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaBasePlugin

import java.nio.file.Paths

/**
 *
 * @author Laszlo Kishalmi
 */
class GatlingPlugin implements Plugin<Project> {

    private final String GATLING_TASK_NAME = 'gatling'

    private final String GATLING_TASK_NAME_PREFIX = "$GATLING_TASK_NAME-"

    private final String GATLING_MAIN_CLASS = 'io.gatling.app.Gatling'

    private Project project

    void apply(Project project) {
        this.project = project

        project.pluginManager.apply ScalaBasePlugin
        project.pluginManager.apply JavaPlugin

        def gatlingExt = project.extensions.create('gatling', GatlingExtension, project)

        createConfiguration(gatlingExt)

        createGatlingTask(GATLING_TASK_NAME, gatlingExt,
                project.sourceSets.gatling.allScala.matching(gatlingExt.simulations).collect { File simu ->
                    Paths.get(new File(project.projectDir, gatlingExt.simulationsDir()).toURI())
                        .relativize(Paths.get(simu.toURI())).join(".") - ".scala"
                }
        )

        project.tasks.addRule('Pattern: gatling-<SimulationClass>: Executes single Gatling simulation.') {
            def taskName ->
                if (taskName.startsWith(GATLING_TASK_NAME_PREFIX)) {
                    createGatlingTask(taskName, gatlingExt, [taskName - GATLING_TASK_NAME_PREFIX])
                }
        }
    }

    protected void createConfiguration(GatlingExtension gatlingExtension) {
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
                scala.srcDirs       = [gatlingExtension.simulationsDir()]
                resources.srcDirs   = [gatlingExtension.dataDir(), gatlingExtension.bodiesDir()]
            }
        }

        project.dependencies {
            gatling "io.gatling.highcharts:gatling-charts-highcharts:${gatlingExtension.toolVersion}"
            
            gatlingCompile project.sourceSets.main.output
            gatlingCompile project.sourceSets.test.output
        }
    }

    def createGatlingTask(String taskName, GatlingExtension gatlingExt, Collection<String> simulations) {
        project.tasks.create(name: taskName, dependsOn: project.tasks.gatlingClasses,
                description: "Execute Gatling simulation", group: "Gatling") << {

            simulations.each { String simu ->
                project.javaexec {
                    main = GATLING_MAIN_CLASS
                    classpath = project.configurations.gatlingRuntime
                    args "-m"
                    args "-bf", "${project.sourceSets.gatling.output.classesDir}"
                    args "-s", simu
                    args "-df", "${project.sourceSets.gatling.output.resourcesDir}"
                    args "-bdf", "${project.sourceSets.gatling.output.resourcesDir}"
                    args "-rf", "${project.reportsDir}/gatling"

                    jvmArgs = gatlingExt.jvmArgs

                    standardInput = System.in
                }
            }
        }
    }
}


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

        def gatlingExt = project.extensions.create('gatling', GatlingPluginExtension, project)

        createConfiguration(gatlingExt)


        project.afterEvaluate { p ->
            createGatlingTask(GATLING_TASK_NAME, gatlingExt,
                p.sourceSets.gatling.allScala.matching(p.gatling.simulations).collect { File simu ->
                    Paths.get(new File(p.projectDir, p.gatling.simulationsDir()).toURI())
                        .relativize(Paths.get(simu.toURI())).join(".") - ".scala"
                }
            )
        }

        project.tasks.addRule('Pattern: gatling-<SimulationClass>: Executes single Gatling simulation.') {
            def taskName ->
                if (taskName.startsWith(GATLING_TASK_NAME_PREFIX)) {
                    createGatlingTask(taskName, gatlingExt, [taskName - GATLING_TASK_NAME_PREFIX])
                }
        }
    }

    protected void createConfiguration(GatlingPluginExtension gatlingExt) {
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
            project.afterEvaluate { p ->
                p.dependencies {
                    gatling "io.gatling.highcharts:gatling-charts-highcharts:${p.gatling.toolVersion}"
                }
            }

            gatlingCompile project.sourceSets.main.output
            gatlingCompile project.sourceSets.test.output
            gatlingRuntime project.files(gatlingExt.confDir())
        }
    }

    def createGatlingTask(String taskName, GatlingPluginExtension gatlingExt, Collection<String> simulations) {
        def task = project.tasks.create(name: taskName, dependsOn: project.tasks.gatlingClasses,
                description: "Execute Gatling simulation", group: "Gatling")
        task.ext.simulations = simulations
        File logConf = project.file(new File(gatlingExt.confDir(), 'logback.xml'))
        File logback = project.file("${project.buildDir}/gatling/logback.xml")
        task.doFirst {
            generateLogConfig(gatlingExt, logback)
        }
        task.doLast {
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

                    systemProperties(System.properties)
                    if (!logConf.isFile()) {
                        systemProperty('logback.configurationFile', logback.absolutePath)
                    }


                    standardInput = System.in
                }
            }
        }
    }

    def generateLogConfig(GatlingPluginExtension gatlingExt, File logback) {
        def template = """<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%-5level] %logger{15} - %msg%n%rEx</pattern>
            <immediateFlush>false</immediateFlush>
        </encoder>
    </appender>
    <root level="${gatlingExt.logLevel}">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
"""
        logback.parentFile.mkdirs()
        logback.text = template
    }
}


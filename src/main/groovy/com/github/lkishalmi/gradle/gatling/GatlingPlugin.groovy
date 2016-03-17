package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaBasePlugin
import org.gradle.plugins.ide.idea.IdeaPlugin

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

        def gatlingExt = project.extensions.create('gatling', GatlingExtension)

        createConfiguration(gatlingExt)
        configureGatlingCompile(gatlingExt)

        createGatlingTask(GATLING_TASK_NAME, gatlingExt,
                project.sourceSets.gatling.allScala.matching(gatlingExt.simulations).collect { File simu ->
                    (simu.absolutePath - (project.projectDir.absolutePath + "/src/gatling/scala/") - ".scala").replaceAll("/", ".")
                }
        )

        project.tasks.addRule('Pattern: gatling-<SimulationClass>: Executes single Gatling simulation.') {
            def taskName ->
                if (taskName.startsWith(GATLING_TASK_NAME_PREFIX)) {
                    createGatlingTask(taskName, gatlingExt, [taskName - GATLING_TASK_NAME_PREFIX])
                }
        }

        project.afterEvaluate {
            def hasIdea = project.plugins.findPlugin(IdeaPlugin)
            if (hasIdea) {
                project.idea {
                    module {
                        scopes.TEST.plus += [project.configurations.gatlingCompile]
                    }
                }
                project.idea {
                    module {
                        project.sourceSets.gatling.scala.srcDirs.each {
                            testSourceDirs += project.file(it)
                        }
                        project.sourceSets.gatling.resources.srcDirs.each {
                            testSourceDirs += project.file(it)
                        }
                    }
                }
            }
        }
    }

    protected void createConfiguration(GatlingExtension gatlingExtension) {
        project.configurations {
            gatlingCompile {
                visible = false
            }
            gatlingRuntime {
                visible = false
            }
        }

        project.sourceSets {
            gatling {
                scala.srcDirs 'src/gatling/scala'
                resources.srcDirs 'src/gatling/resources'
            }
        }

        project.dependencies {
            gatlingCompile "io.gatling.highcharts:gatling-charts-highcharts:${gatlingExtension.toolVersion}"

            gatlingCompile project.sourceSets.main.output
            gatlingCompile project.sourceSets.test.output
        }
    }

    def configureGatlingCompile(GatlingExtension gatling) {
        def scalaCompile = project.tasks["compileGatlingScala"]
        scalaCompile.conventionMapping.with {
            description = { "Compiles Gatling simulations." }
        }
        project.gradle.projectsEvaluated {
            scalaCompile.scalaCompileOptions.incrementalOptions.with {
                if (!analysisFile) {
                    analysisFile = new File("$project.buildDir/tmp/scala/compilerAnalysis/${scalaCompile.name}.analysis")
                }
            }
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
                    args "-df", "${project.sourceSets.gatling.output.resourcesDir}/data"
                    args "-bdf", "${project.sourceSets.gatling.output.resourcesDir}/bodies"
                    args "-rf", "${project.reportsDir}/gatling"

                    jvmArgs = gatlingExt.jvmArgs

                    standardInput = System.in
                }
            }
        }
    }
}


package com.github.lkishalmi.gradle.gatling

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.JavaExec
import org.gradle.util.GradleVersion

class GatlingRunTask extends JavaExec {

    private final String GATLING_MAIN_CLASS = 'io.gatling.app.Gatling'

    def simulations

    List<String> jvmArgs

    public GatlingRunTask() {

        main = GATLING_MAIN_CLASS
        classpath = project.configurations.gatlingRuntime

        args "-m"
        if (GradleVersion.current() >= GradleVersion.version('4.0')) {
            File scalaClasses = project.sourceSets.gatling.output.classesDirs.filter {
                it.parentFile.name == 'scala'
            }.singleFile

            args '-bf', scalaClasses.absolutePath
        } else {
            args "-bf", "${project.sourceSets.gatling.output.classesDir}"
        }
        args "-df", "${project.sourceSets.gatling.output.resourcesDir}"
        args "-bdf", "${project.sourceSets.gatling.output.resourcesDir}"
        args "-rf", "${project.reportsDir}/gatling"

        systemProperties = System.properties as Map
        standardInput = System.in
    }

    @Override
    void exec() {
        def self = this

        Iterable<String> actualSimulations
        if (getSimulations() instanceof Closure<Iterable<String>>) {
            actualSimulations = project.extensions.getByType(GatlingPluginExtension).resolveSimulations(getSimulations())
        } else if (getSimulations() instanceof Iterable<String>) {
            actualSimulations = getSimulations()
        } else {
            throw new IllegalArgumentException("`simulations` property neither Closure nor Iterable<String>")
        }

        actualSimulations.each { def simu ->
            project.javaexec {
                main = self.getMain()
                classpath = self.getClasspath()

                jvmArgs = self.getJvmArgs()

                args self.getArgs()
                args "-s", simu

                systemProperties = self.getSystemProperties()
                standardInput = self.getStandardInput()
            }
        }
    }
}

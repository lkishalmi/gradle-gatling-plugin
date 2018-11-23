package com.github.lkishalmi.gradle.gatling

import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.util.GradleVersion

class GatlingRunTask extends JavaExec {

    private final String GATLING_MAIN_CLASS = 'io.gatling.app.Gatling'

    def simulations

    List<String> jvmArgs

    public GatlingRunTask() {

        main = GATLING_MAIN_CLASS
        classpath = project.configurations.gatlingRuntime

        if (GradleVersion.current() >= GradleVersion.version('4.0')) {
            File scalaClasses = project.sourceSets.gatling.output.classesDirs.filter {
                it.parentFile.name == 'scala'
            }.singleFile

            args '-bf', scalaClasses.absolutePath
        } else {
            args "-bf", "${project.sourceSets.gatling.output.classesDir}"
        }

        args "-rsf", "${project.sourceSets.gatling.output.resourcesDir}"
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

        def failures = [:]

        actualSimulations.each { simu ->
            try {
                project.javaexec {
                    main = self.getMain()
                    classpath = self.getClasspath()

                    jvmArgs = self.getJvmArgs()

                    args self.getArgs()
                    args "-s", simu

                    systemProperties = self.getSystemProperties()
                    standardInput = self.getStandardInput()
                }
            } catch (Exception e) {
                failures << [(simu): e]
            }
        }

        if (failures.size() > 0) {
            throw new TaskExecutionException(this, new RuntimeException("Some simulations failed : ${failures.keySet().join(", ")}"))
        }
    }
}

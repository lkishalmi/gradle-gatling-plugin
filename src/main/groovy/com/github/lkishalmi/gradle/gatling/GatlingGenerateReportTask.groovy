package com.github.lkishalmi.gradle.gatling

import org.gradle.api.tasks.JavaExec

class GatlingGenerateReportTask extends JavaExec {

    public GatlingGenerateReportTask() {
        main = GatlingPlugin.GATLING_MAIN_CLASS
        classpath = project.configurations.gatlingRuntime
    }

    @Override
    void exec() {
        project.javaexec {
            main = this.getMain()
            classpath = this.getClasspath()

            def folder = System.getProperty("resultFolder")
            args "-ro", folder
        }
    }
}

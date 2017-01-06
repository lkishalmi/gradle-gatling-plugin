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

            def resultFolder = project.extensions.getByType(GatlingPluginExtension).resultFolder
            if( resultFolder == null ) {
                throw new IllegalArgumentException("`resultFolder` needs to be defined in the Closure")
            } else if( !(new File(resultFolder)).exists() ) {
                throw new IllegalArgumentException("The folder '"+resultFolder+"' does not exist")
            }
            // Generate reports
            args "-ro", resultFolder
        }
    }
}
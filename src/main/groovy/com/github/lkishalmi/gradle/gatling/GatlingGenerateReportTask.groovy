package com.github.lkishalmi.gradle.gatling

import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.JavaExec

class GatlingGenerateReportTask extends JavaExec {

    GatlingGenerateReportTask() {
        main = GatlingPlugin.GATLING_MAIN_CLASS
        classpath = project.configurations.gatlingRuntime
    }

    @InputDirectory
    File getResultFolder() {
        project.extensions.getByType(GatlingPluginExtension).resultFolder
    }

    @Override
    void exec() {
        project.javaexec {
            main = this.getMain()
            classpath = this.getClasspath()

            if( getResultFolder() == null ) {
                throw new IllegalArgumentException("`resultFolder` needs to be defined in the Closure")
            } else if( !getResultFolder().exists() ) {
                throw new IllegalArgumentException("The folder '"+resultFolder+"' does not exist")
            }
            // Generate reports
            args "-ro", resultFolder
        }
    }
}
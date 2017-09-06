package com.github.lkishalmi.gradle.gatling

import org.gradle.api.Action
import org.gradle.api.Task

public class LogbackConfigTaskAction implements Action<Task> {
    static def template(String logLevel) {
        """<?xml version="1.0" encoding="UTF-8"?>
            <configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
         <immediateFlush>false</immediateFlush>
    </appender>
    <root level="${logLevel}">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
            """
    }

    static def templateLegacy(String logLevel) {
        """<?xml version="1.0" encoding="UTF-8"?>
            <configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <immediateFlush>false</immediateFlush>
        </encoder>
    </appender>
    <root level="${logLevel}">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
            """
    }

    void execute(Task gatlingRunTask) {
        def gatlingExt = gatlingRunTask.project.extensions.getByType(GatlingPluginExtension)
        if (!gatlingRunTask.project.file("${gatlingExt.confDir()}/logback.xml").exists()) {

            // gatling 2.3.x updated logback and the config file has changed therefore we must differentiate between >= 2.3 and <2.3
            // see https://github.com/gatling/gatling/commit/6a35dbaa8607deda4a20abf1e8a553d9cd9cef01 for details
            if (gatlingExt.toolVersion.startsWith("2.3")) {
                new File(gatlingRunTask.project.buildDir, "resources/gatling/logback.xml").with {
                    parentFile.mkdirs()
                    text = template(gatlingExt.logLevel)
                }
            } else {
                new File(gatlingRunTask.project.buildDir, "resources/gatling/logback.xml").with {
                    parentFile.mkdirs()
                    text = templateLegacy(gatlingExt.logLevel)
                }
            }

        }
    }
}

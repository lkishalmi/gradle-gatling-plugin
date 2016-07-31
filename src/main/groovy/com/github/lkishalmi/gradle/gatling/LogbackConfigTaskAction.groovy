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
            new File(gatlingRunTask.project.buildDir, "resources/gatling/logback.xml").with {
                parentFile.mkdirs()
                text = template(gatlingExt.logLevel)
            }
        }
    }
}

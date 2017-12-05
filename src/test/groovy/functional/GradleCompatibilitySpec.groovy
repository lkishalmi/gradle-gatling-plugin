package functional

import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GradleCompatibilitySpec extends GatlingFuncSpec {

    @Unroll
    void 'use #dirType for Gradle version #gradleVersion'() {
        setup:
        prepareTest('gradle')

        when:
        BuildResult result = executeGradle('tasks',gradleVersion)

        then:
        result.task(":tasks").outcome == SUCCESS
        !result.output.contains('Gradle now uses separate output directories for each JVM language, but this build assumes a single directory for all classes from a source set. This behaviour has been deprecated and is scheduled to be removed in Gradle 5.0')

        where:
        gradleVersion | dirType
        '3.5'         | 'classesDir'
        '4.0'         | 'classesDirs'
        '4.3'         | 'classesDirs'

    }
}
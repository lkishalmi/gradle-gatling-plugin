package functional

import helper.GatlingFuncSpec
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.gradle.testkit.runner.UnsupportedFeatureException
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GradleCompatibilitySpec extends GatlingFuncSpec {

    BuildResult executeGradleTaskWithVersion(String task, String gradleVersion) {
        GradleRunner.create().forwardOutput()
            .withProjectDir(projectDir.root)
            .withArguments("--stacktrace", GATLING_HOST_NAME_SYS_PROP, task)
            .withPluginClasspath()
            .withDebug(true)
            .withGradleVersion(gradleVersion)
            .build()
    }

    @Unroll
    void 'use #dirType for Gradle version #gradleVersion'() {
        given:
        prepareTest()
        when:
        BuildResult result = executeGradleTaskWithVersion('tasks', gradleVersion)
        then:
        result.task(":tasks").outcome == SUCCESS
        !result.output.contains('Gradle now uses separate output directories for each JVM language, but this build assumes a single directory for all classes from a source set. This behaviour has been deprecated and is scheduled to be removed in Gradle 5.0')
        where:
        gradleVersion | dirType
        '3.5'         | 'classesDir'
        '4.0'         | 'classesDirs'
        '4.3'         | 'classesDirs'
    }

    @Unroll
    void 'should fail for gradle less than 3.0, #gradleVersion'() {
        given:
        prepareTest()
        when:
        executeGradleTaskWithVersion('tasks', gradleVersion)
        then:
        thrown(ex)
        where:
        gradleVersion || ex
        "2.14"         | UnexpectedBuildFailure
        "1.11"         | UnsupportedFeatureException
    }
}

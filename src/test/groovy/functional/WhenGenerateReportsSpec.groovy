package functional

import org.gradle.testkit.runner.BuildResult
import spock.lang.Unroll

import static com.github.lkishalmi.gradle.gatling.GatlingPlugin.GATLING_GENERATE_REPORT_TASK_NAME
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

/**
 * Created by pedrogonzalezgutierrez on 09/01/2017.
 */
public class WhenGenerateReportsSpec extends GatlingFuncSpec {

    @Unroll
    def "should not fail when resultFolder is defined in closure of the build.gradle, layout `#layout`"() {

        setup:
        prepareGenerateReportTest(layout)

        when:
        BuildResult result = executeGradle(GATLING_GENERATE_REPORT_TASK_NAME)

        then: "task executed successfully"
        result.task(":$GATLING_GENERATE_REPORT_TASK_NAME").outcome == SUCCESS

        where:
        layout << [ 'gradle', 'gatling' ]
    }

}
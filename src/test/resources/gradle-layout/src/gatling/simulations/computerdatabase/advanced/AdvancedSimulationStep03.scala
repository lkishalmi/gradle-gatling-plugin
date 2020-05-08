package computerdatabase.advanced

import org.apache.commons.lang.StringUtils.lowerCase
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class AdvancedSimulationStep03 extends Simulation {

  object Search {

    // We need dynamic data so that all users don't play the same and we end up with a behavior completely different from the live system (caching, JIT...)
    // ==> Feeders!

    val feeder = csv("search.csv").random // default is queue, so for this test, we use random to avoid feeder starvation

    val search = exec(http("Home").get("/"))
      .pause(1)
      .feed(feeder) // every time a user passes here, a record is popped from the feeder and injected into the user's session
      .exec(http("Search")
      .get("/computers?f=${searchCriterion}") // use session data thanks to Gatling's EL
      .check(css("a:contains('${searchComputerName}')", "href"))) // use a CSS selector with an EL, save the result of the capture group
  }

  val httpConf = http
    .baseUrl(lowerCase(computerdatabase.MainUtils.hostName()))
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val users = scenario("Users").exec(Search.search)

  setUp(users.inject(atOnceUsers(1))).protocols(httpConf).assertions(
    global.successfulRequests.percent.gt(99)
  )
}

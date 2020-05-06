import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class SampleSimulation extends Simulation {

  object Search {

    val feeder = csv("search.csv").random

    val search = exec(
      http("Home")
        .get("/")
    ).pause(1)
      .feed(feeder)
      .exec(
        http("Search")
          .get("/computers?f=${searchCriterion}")
          .check(css("a:contains('${searchComputerName}')", "href").saveAs("computerURL"))
      )
      .pause(1)
      .exec(
        http("Select")
          .get("${computerURL}")
          .check(status.is(200))
      )
      .pause(1)
  }

  val httpProtocol = http
    .baseUrl("http://computer-database.gatling.io")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val users = scenario("Users").exec(Search.search)

  setUp(users.inject(rampUsers(1) during (1 seconds))).protocols(httpProtocol)
}

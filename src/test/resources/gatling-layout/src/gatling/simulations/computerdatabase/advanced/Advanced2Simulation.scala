package computerdatabase.advanced

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class Advanced2Simulation extends Simulation {

  // Let's split this big scenario into composable business processes, like one would do with PageObject pattern with Selenium

  // object are native Scala singletons
  object Search {

    val search = exec(http("Home") // let's give proper names, they are displayed in the reports, and used as keys
        .get("/"))
      .pause(1) // let's set the pauses to 1 sec for demo purpose
      .exec(http("Search")
        .get("/computers?f=macbook"))
      .pause(1)
      .exec(http("Select")
        .get("/computers/6"))
      .pause(1)
  }

  val httpConf = http
    .baseURL("http://computer-database.gatling.io")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  // Now, we can write the scenario as a composition
  val scn = scenario("Scenario Name").exec(Search.search)

  setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
}

package tdar

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._
import scala.concurrent.duration._
import bootstrap._
import assertions._

class TdarSimulation extends Simulation {

	val httpProtocol = http
		.baseURL("http://ec2-54-186-126-67.us-west-2.compute.amazonaws.com")
		.acceptCharsetHeader("ISO-8859-1,utf-8;q=0.7,*;q=0.7")
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate")
		.disableFollowRedirect

	val headers_1 = Map(
		"Keep-Alive" -> "115")

	val headers_3 = Map(
		"Keep-Alive" -> "115",
		"Content-Type" -> "application/x-www-form-urlencoded")

    val documents = csv("documents.csv").random

	val scn = scenario("tDAR traffic mockup")
		.group("AccessAndBrowse") {
			exec(
				http("HomePage")
					.get("/")
					.headers(headers_1)
					.check(status.is(200))
			)
			.pause(100 milliseconds, 200 milliseconds)
				.exec(
					http("BrowseResults")
						.get("/search/results")
						.headers(headers_1)
						.check(status.is(200))
					)
				.pause(0 milliseconds, 100 milliseconds)
				.repeat(5) {
    			  feed(documents)
      				.exec(
        				http("View a random document")
          				.get("/document/${documentId}")
          				.check(status.is(200))
          			)
          			.pause(6, 9)
    				}
    		}

	setUp(scn.inject(ramp(30 users) over (60 seconds)))
	// setUp(scn.inject(constantRate(3 usersPerSec) during (15 seconds)))
	// setUp(scn.inject(nothingFor(30 seconds), ramp(30 users) over (60 seconds)))
	// setUp(scn.inject(rampRate(1 usersPerSec) to(1 usersPerSec) during(1 minutes)))
		.protocols(httpProtocol)
}

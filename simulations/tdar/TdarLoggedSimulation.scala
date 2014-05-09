package tdar

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import io.gatling.http.Headers.Names._
import scala.concurrent.duration._
import bootstrap._
import assertions._

class TdarLoggedSimulation extends Simulation {

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
    		}
    	.group("UserLogged") {			
				exec(
					http("AccessLoginPage")
						.get("/login")
						.headers(headers_1)
				)
				.pause(12, 13)
				.feed(csv("tdar_credentials.csv"))
				.exec(
					http("Login")
						.post("/login/process")
						.param("loginUsername", "${username}")
						.param("loginPassword", "${password}")
						.headers(headers_3)
						.check(status.is(302))
				)		
		.pause(0 milliseconds, 100 milliseconds)
		// .repeat(2) {
			.exec(
				http("Dashboard")
					.get("/dashboard")
					.headers(headers_1)
					.check(status.is(200))
					.check(regex("""<h1>(.*)'s Dashboard</h1>""").exists)
				)
				.pause(7, 8)
				.exec(
					http("CreatingNewResource")
						.get("/resource/add")
						.headers(headers_1)
						.check(regex("""<h1>Create &amp; <span>Organize</span> Resources</h1>""").exists)
					)
				.pause(100 milliseconds, 200 milliseconds)
				.exec(
					http("SearchForArcheology")
						.get("/search/results?query=archeology")
						.headers(headers_1)
						.check(status.is(200))
					)
				.pause(5, 6)
				.exec(
					http("CreatingNewProject")
						.get("/project/add")
						.headers(headers_1)
						.check(regex("""<h1>Creating:<span> New Project </span></h1>""").exists)
					)
		// }
		.exec(
			http("Logout")
				.get("/logout")
				.headers(headers_1)
				.check(status.is(302))
		)
	}
		.pause(0 milliseconds, 100 milliseconds)
		.exec(
			http("LoginAgain")
				.get("/login")
				.headers(headers_1)
				.check(status.is(200))
		)

	setUp(scn.inject(ramp(5 users) over (3 seconds)))
	// setUp(scn.inject(rampRate(1 usersPerSec) to(10 usersPerSec) during(1 minutes)))
		.protocols(httpProtocol)
}

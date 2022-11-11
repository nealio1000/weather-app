package org.neal

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import org.neal.apis._

object WeatherServer extends IOApp with LazyLogging {
  final val httpApp = {
    Router[IO](
      "/health" -> HealthApi.service,
      "/weather" -> Logger.httpRoutes(logHeaders = true, logBody = true)(WeatherApi.service)
    ).orNotFound
  }

  def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO]
      .bindHttp(8081, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}

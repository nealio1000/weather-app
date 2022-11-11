package org.neal.apis

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.server.middleware.CORS
import org.http4s.{HttpRoutes, Response}

object HealthApi extends LazyLogging {
  val service: HttpRoutes[IO] = CORS.policy.withAllowOriginAll(HttpRoutes.of[IO] {
    case GET -> Root => checkHealth
  })

  def checkHealth: IO[Response[IO]] = {
    logger.debug("HealthChecked OK")
    Ok(Json.obj(("ok", true.asJson)))
  }
}

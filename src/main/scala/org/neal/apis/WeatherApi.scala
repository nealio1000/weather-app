package org.neal.apis

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax._
import org.http4s.circe._
import io.circe.generic.auto._
import org.http4s.dsl.io._
import org.http4s.server.middleware.CORS
import org.http4s._
import org.neal.functions.WeatherApiFunctions

/**
 * API that makes requests to NWS API
 */
class WeatherApi(wf: WeatherApiFunctions) extends LazyLogging {
  val service: HttpRoutes[IO] = CORS.policy.withAllowOriginAll(HttpRoutes.of[IO] {
    case GET -> Root :? LatitudeQueryParamMatcher(lat) :? LongitudeQueryParamMatcher(lon) =>
      getWeatherForLatLon(lat = lat, lon = lon)
  })

  /**
   * Get short forecast and temperature from NWS API using lat/lon pair
   * @param lat latitude
   * @param lon longitude
   * @return IO[Response] with the result of the lookup
   */
  def getWeatherForLatLon(lat: Double, lon: Double): IO[Response[IO]] = {
    wf.getGridCoordinates(lat, lon).attempt.flatMap {
      case Left(ex) =>
        logger.error(s"Failed to get grid coordinates for lat/lon: $lat,$lon")
        InternalServerError(ex.getMessage.asJson)
      case Right(gridCoordinates) =>
        val gridId = gridCoordinates.gridId
        val gridX = gridCoordinates.gridX
        val gridY = gridCoordinates.gridY

        wf.getForecast(gridId, gridX, gridY).attempt.flatMap {
          case Left(ex) =>
            logger.error(s"Failed to get forecast for gridId: $gridId, gridX: $gridX, gridY: $gridY", ex)
            InternalServerError(ex.getMessage.asJson)
          case Right(forecast) =>
            Ok(forecast.asJson)
        }
    }
  }
}

// Query Parameter Matchers
object LatitudeQueryParamMatcher extends QueryParamDecoderMatcher[Double]("lat")
object LongitudeQueryParamMatcher extends QueryParamDecoderMatcher[Double]("lon")
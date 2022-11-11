package org.neal.apis

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import io.circe.syntax._
import org.http4s.circe._
import io.circe.generic.auto._
import org.http4s.dsl.io._
import org.http4s.server.middleware.CORS
import org.http4s._
import org.http4s.blaze.client.BlazeClientBuilder
import org.neal.models._
import org.http4s.client.dsl.io._
import org.http4s.headers.{Accept, `User-Agent`}

object WeatherApi extends LazyLogging {
  val service: HttpRoutes[IO] = CORS.policy.withAllowOriginAll(HttpRoutes.of[IO] {
    case GET -> Root :? LatitudeQueryParamMatcher(lat) :? LongitudeQueryParamMatcher(lon) =>
      getWeatherForLatLon(lat = lat, lon = lon)
  })

  def getWeatherForLatLon(lat: Double, lon: Double): IO[Response[IO]] = {
    getGridCoordinates(lat, lon).attempt.flatMap {
      case Left(ex) =>
        logger.error(s"Failed to get grid coordinates for lat/lon: $lat,$lon")
        throw ex
      case Right(gridCoordinates) =>
        val gridId = gridCoordinates.gridId
        val gridX = gridCoordinates.gridX
        val gridY = gridCoordinates.gridY

        getForecast(gridId, gridX, gridY).attempt.flatMap {
          case Left(ex) =>
            logger.error(
              s"Failed to get forecast for gridId: $gridId, gridX: $gridX, gridY: $gridY",
              ex
            )
            InternalServerError(ex.getMessage.asJson)
          case Right(forecast) =>
            Ok(forecast.asJson)
        }
    }
  }

  def toTemp: Int => String = (i: Int) => {
    if(i <= 32) "Cold"
    else if(i > 32 && i < 75) "Moderate"
    else "Hot"
  }

  def getForecast(gridId: String, gridX: Int, gridY: Int): IO[Forecast] = {
    BlazeClientBuilder[IO].resource.use { client =>
      val urlString = s"https://api.weather.gov/gridpoints/$gridId/$gridX,$gridY/forecast"
      val target: Uri = Uri.fromString(urlString) match {
        case Left(ex) =>
          logger.error(s"Failed to build forecast target uri: $urlString", ex)
          throw ex
        case Right(uri) => uri
      }

      val request: Request[IO] = Request(
        uri = target,
        headers = Headers(Header.Raw(`User-Agent`.name, "nealiof1000@gmail.com"))
      )

      for {
        json <- client.expect[Json](request)
      } yield {
        val props = json.hcursor.downField("properties")
        val todaysForecast: Json = props.get[List[Json]]("periods") match {
          case Left(ex) =>
            logger.error(s"Failed to decode periods for gridId: $gridId and gridX/Y: $gridX, $gridY")
            throw ex
          case Right(periods) => periods.headOption match {
            case Some(period) => period
            case None =>
              val ex = new Exception("Forecast cannot have empty periods")
              logger.error(s"Forecast cannot have empty periods", ex)
              throw ex
          }
        }

        val shortForecast = todaysForecast.hcursor.get[String]("shortForecast") match {
          case Left(ex) =>
            logger.error(s"Failed to decode shortForecast", ex)
            throw ex
          case Right(sf) => sf
        }
        val temperature = todaysForecast.hcursor.get[Int]("temperature") match {
          case Left(ex) =>
            logger.error(s"Failed to decode temperature", ex)
            throw ex
          case Right(t) => toTemp(t)
        }

        Forecast(
          shortForecast = shortForecast,
          temperature = temperature
        )
      }
    }
  }

  def getGridCoordinates(lat: Double, lon: Double): IO[GridCoordinates] = {
    BlazeClientBuilder[IO].resource.use { client =>
      val urlString = s"https://api.weather.gov/points/$lat,$lon"
      val target = Uri.fromString(urlString) match {
        case Left (ex) =>
          logger.error (s"Failed to build grid coordinates target uri: $urlString", ex)
          throw ex
        case Right(uri) => uri
      }

      val request: Request[IO] = Request(
        uri = target,
        headers = Headers(Header.Raw(`User-Agent`.name, "nealiof1000@gmail.com"))
      )

      for {
        json <- client.expect[Json](request).attempt.flatMap {
          case Left(value) => throw value
          case Right(value) => IO.pure(value)
        }
      } yield {
        val props = json.hcursor.downField("properties")
        val gridId = props.get[String]("gridId") match {
          case Left(ex) =>
            logger.error(s"Failed to decode gridId for lat/lon: $lat,$lon", ex)
            throw ex
          case Right(id) => id
        }
        val gridX = props.get[Int]("gridX") match {
          case Left(ex) =>
            logger.error(s"Failed to decode gridX for lat/lon: $lat,$lon", ex)
            throw ex
          case Right(x) => x
        }

        val gridY = props.get[Int]("gridY") match {
          case Left(ex) =>
            logger.error(s"Failed to decode gridY for lat/lon: $lat,$lon", ex)
            throw ex
          case Right(y) => y
        }

        GridCoordinates(
          gridId = gridId,
          gridX = gridX,
          gridY = gridY
        )
      }
    }
  }
}

object LatitudeQueryParamMatcher extends QueryParamDecoderMatcher[Double]("lat")
object LongitudeQueryParamMatcher extends QueryParamDecoderMatcher[Double]("lon")
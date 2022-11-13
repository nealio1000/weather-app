package org.neal.functions

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.neal.models._
import org.http4s.circe._

class WeatherApiFunctions() extends LazyLogging {
  /**
   * Helper function that defines our temperature mapping
   *
   * @return Hot/Cold/Moderate
   */
  def toTemp: Int => String = (i: Int) => {
    if (i <= 32) "Cold"
    else if (i > 32 && i <= 75) "Moderate"
    else "Hot"
  }

  /**
   * Use grid info to get forecast details from NWS API
   *
   * @param gridId the gridId
   * @param gridX  x point
   * @param gridY  y point
   * @return Forecast Object with short forecast and temperature
   */
  def getForecast(gridId: String, gridX: Int, gridY: Int): IO[Forecast] = {
    BlazeClientBuilder[IO].resource.use { client =>
      val urlString = s"https://api.weather.gov/gridpoints/$gridId/$gridX,$gridY/forecast"
      val target: Uri = Uri.fromString(urlString) match {
        case Left(ex) =>
          logger.error(s"Failed to build forecast target uri: $urlString", ex)
          throw ex
        case Right(uri) => uri
      }

      for {
        json <- client.expect[Json](uri = target).attempt.flatMap {
          case Left(ex) =>
            logger.error(s"Failed to call $urlString", ex)
            throw ex
          case Right(result) => IO.pure(result)
        }
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

  /**
   * Get grid coordinates from a lat/lon pair using NWS API
   *
   * @param lat latitude
   * @param lon longitude
   * @return GridCoordinates object with gridId, gridX, and gridY
   */
  def getGridCoordinates(lat: Double, lon: Double): IO[GridCoordinates] = {
    BlazeClientBuilder[IO].resource.use { client =>
      val urlString = s"https://api.weather.gov/points/$lat,$lon"
      val target = Uri.fromString(urlString) match {
        case Left(ex) =>
          logger.error(s"Failed to build grid coordinates target uri: $urlString", ex)
          throw ex
        case Right(uri) => uri
      }

      for {
        json <- client.expect[Json](uri = target).attempt.flatMap {
          case Left(ex) =>
            logger.error(s"Failed to call $urlString", ex)
            throw ex
          case Right(result) => IO.pure(result)
        }
      } yield {
        val statusOpt = json.hcursor.get[Int]("status").toOption

        if(statusOpt.isEmpty) {
          val props = json.hcursor.downField("properties")

          val gridId = props.get[Option[String]]("gridId") match {
            case Left(ex) =>
              logger.error(s"Failed to decode gridId for lat/lon: $lat,$lon", ex)
              throw ex
            case Right(idOpt) => idOpt match {
              case Some(id) => id
              case None =>
                logger.error(s"GridId returned from NWS as null for lat/lon: $lat,$lon")
                throw new Exception(s"GridId returned from NWS as null for lat/lon: $lat,$lon")
            }
          }

          val gridX = props.get[Option[Int]]("gridX") match {
            case Left(ex) =>
              logger.error(s"Failed to decode gridX for lat/lon: $lat,$lon", ex)
              throw ex
            case Right(xOpt) => xOpt match {
              case Some(x) => x
              case None =>
                logger.error(s"gridX returned from NWS as null for lat/lon: $lat,$lon")
                throw new Exception(s"gridX returned from NWS as null for lat/lon: $lat,$lon")
            }
          }

          val gridY = props.get[Option[Int]]("gridY") match {
            case Left(ex) =>
              logger.error(s"Failed to decode gridY for lat/lon: $lat,$lon", ex)
              throw ex
            case Right(yOpt) => yOpt match {
              case Some(y) => y
              case None =>
                logger.error(s"gridY returned from NWS as null for lat/lon: $lat,$lon")
                throw new Exception(s"gridY returned from NWS as null for lat/lon: $lat,$lon")
            }
          }

          GridCoordinates(
            gridId = gridId,
            gridX = gridX,
            gridY = gridY
          )
        } else {
          logger.error(s"Data Unavailable For Requested Point: $lat, $lon")
          throw new Exception(s"Data Unavailable For Requested Point: $lat, $lon")
        }
      }
    }
  }
}

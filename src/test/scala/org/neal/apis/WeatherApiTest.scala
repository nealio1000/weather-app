package org.neal.apis

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.typesafe.scalalogging.LazyLogging
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{Method, Request}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.neal.functions.WeatherApiFunctions
import org.neal.models.{Forecast, GridCoordinates}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar

class WeatherApiTest extends AnyFlatSpecLike with Matchers with LazyLogging with MockitoSugar {
  // ingest mock api functions
  private val mockFuncs = mock[WeatherApiFunctions]
  private val api = new WeatherApi(mockFuncs).service.orNotFound
  private val gridCoordinates = GridCoordinates(gridId = "TOP", gridX = 31, gridY = 80)
  private val forecast = Forecast("Sunny", "Hot")

  behavior of "getWeatherForLatLon"

  it should "return a 200 when both NWS calls succeed" in {
    when(mockFuncs.getGridCoordinates(any[Double], any[Double])).thenReturn(
      IO.pure(gridCoordinates)
    )
    when(mockFuncs.getForecast(any[String], any[Int], any[Int])).thenReturn(
      IO.pure(forecast)
    )

    val request = Request[IO](method = Method.GET, uri = uri"?lat=20.7456&lon=0")
    val response = api.run(request).unsafeRunSync()
    response.status.code mustEqual 200
  }

  it should "return a 500 if there is an error during getGridCoordinates" in {
    when(mockFuncs.getGridCoordinates(any[Double], any[Double])).thenReturn(
      IO.raiseError(new Exception("something bad!"))
    )
    when(mockFuncs.getForecast(any[String], any[Int], any[Int])).thenReturn(
      IO.pure(forecast)
    )

    val request = Request[IO](method = Method.GET, uri = uri"?lat=20.7456&lon=0")
    val response = api.run(request).unsafeRunSync()
    response.status.code mustEqual 500
  }

  it should "return a 500 if there is an error during getForecast" in {
    when(mockFuncs.getGridCoordinates(any[Double], any[Double])).thenReturn(
      IO.pure(gridCoordinates)
    )
    when(mockFuncs.getForecast(any[String], any[Int], any[Int])).thenReturn(
      IO.raiseError(new Exception("something bad!"))
    )

    val request = Request[IO](method = Method.GET, uri = uri"?lat=20.7456&lon=0")
    val response = api.run(request).unsafeRunSync()
    response.status.code mustEqual 500
  }
}

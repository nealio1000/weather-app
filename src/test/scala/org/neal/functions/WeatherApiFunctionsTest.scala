package org.neal.functions

import cats.effect.unsafe.implicits.global
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.neal.models.{Forecast, GridCoordinates}


// TODO make client injectable in WeatherApiFunctions and use a mock client here
class WeatherApiFunctionsTest extends AnyFlatSpecLike with Matchers with LazyLogging with MockitoSugar {

  private val api = new WeatherApiFunctions()

  it should "successfully return a forecast" in {
    val result: Forecast = api.getForecast("TOP", 31, 80).unsafeRunSync()
    result mustEqual Forecast("Mostly Cloudy", "Moderate")
  }

  it should "fail as expected to return a forecast if points arent in the grid" in {
    assertThrows[Exception](api.getForecast("INVALID!", 0, 0).unsafeRunSync())
  }

  it should "successfully return grid coordinates" in {
    val result: GridCoordinates = api.getGridCoordinates(lat = 30.7456,lon = -97.0892).unsafeRunSync()
    result mustEqual GridCoordinates("FWD", 76, 13)
  }

  it should "fail as expected to return grid coordinates" in {
    assertThrows[Exception](api.getGridCoordinates(20.7456,0).unsafeRunSync())
  }

  it should "set a cold temp to be cold" in {
    val temp = 0
    api.toTemp(temp) mustEqual "Cold"
  }

  it should "set a moderate temp to be moderate" in {
    val temp = 50
    api.toTemp(temp) mustEqual "Moderate"
  }

  it should "set a hot temp to be hot" in {
    val temp = 100
    api.toTemp(temp) mustEqual "Hot"
  }
}

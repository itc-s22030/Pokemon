package jp.ac.it_college.std.s22030.weatherapi.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class WeatherResponse(
    @SerialName("city") val city: City,
    @SerialName("list") val forecast: List<WeatherList>
)

@Serializable
data class City(
    val name: String,
)


@Serializable
data class WeatherList(
    val dt_txt: String,
    @SerialName("main") val main: Main,
    @SerialName("weather") val weather: List<Weather>,
    @SerialName("wind") val wind: Wind,
)

@Serializable
data class Main(
    val temp: Float,
    val feels_like: Float,
    val grnd_level: Int,
    val humidity: Int,
)

@Serializable
data class Weather(
    val main: String,
    val description: String,
    val icon: String,
)

@Serializable
data class Wind(
    val speed: Float,
    val deg: Int,
    val gust: Float,
)

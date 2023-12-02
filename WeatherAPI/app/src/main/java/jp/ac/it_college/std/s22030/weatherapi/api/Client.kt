package jp.ac.it_college.std.s22030.weatherapi.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
object Client {

    private val ktor = HttpClient(CIO) {
        engine {
            endpoint {
                connectTimeout = 5000
                requestTimeout = 5000
                socketTimeout = 5000
            }
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }

    suspend fun get(param: Int): WeatherResponse {
        return ktor.get { url("http://api.openweathermap.org/data/2.5/forecast?id=${param}&appid=${BuildConfig.API_KEY}&lang=ja&units=metric") }
            .body()
    }

    suspend fun getFromLocation(lat: Double?, longi: Double?): WeatherResponse {
        return ktor.get { url("https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$longi&appid=${BuildConfig.API_KEY}&lang=ja&units=metric") }
            .body()
    }

}
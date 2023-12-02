package jp.ac.it_college.std.s22030.weatherapi.api

import io.ktor.client.call.body
import jp.ac.it_college.std.s22030.weatherapi.api.model.Weather

object GamesGroup {
    /**
     * 世代情報を取る
     */
    suspend fun getGeneration(gen: Int): Generation {
        return Client.get(Weather).body()
    }
}
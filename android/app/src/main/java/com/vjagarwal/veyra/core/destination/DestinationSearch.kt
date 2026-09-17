package com.vjagarwal.veyra.core.destination

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object DestinationSearch {
    data class Result(
        val name: String,
        val displayName: String,
        val latitude: Double,
        val longitude: Double
    )

    suspend fun search(query: String): List<Result> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext emptyList()

        val encoded = URLEncoder.encode(trimmed, "UTF-8")
        val url = URL(
            "https://nominatim.openstreetmap.org/search?format=jsonv2&limit=5&countrycodes=in&q=$encoded"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("User-Agent", "Veyra/1.0 (https://github.com/vjagarwal1144-cloud/Veyra)")
            setRequestProperty("Accept-Language", "en-IN,en;q=0.9")
        }

        try {
            if (connection.responseCode !in 200..299) return@withContext emptyList()
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(body)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val latitude = item.optString("lat").toDoubleOrNull() ?: continue
                    val longitude = item.optString("lon").toDoubleOrNull() ?: continue
                    val display = item.optString("display_name").trim()
                    if (display.isBlank()) continue
                    val name = item.optString("name").takeIf { it.isNotBlank() } ?: display.substringBefore(",")
                    add(Result(name, display, latitude, longitude))
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}

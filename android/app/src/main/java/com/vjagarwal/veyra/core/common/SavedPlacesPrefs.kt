package com.vjagarwal.veyra.core.common

import android.content.Context
import java.net.URLDecoder
import java.net.URLEncoder

/** Small offline-first store for the user's favourite destinations. */
class SavedPlacesPrefs(context: Context) {
    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    data class Place(val name: String, val latitude: Double, val longitude: Double)

    fun list(): List<Place> = buildList {
        repeat(MAX) { index ->
            val raw = prefs.getString(key(index), null) ?: return@repeat
            val parts = raw.split('|')
            if (parts.size == 3) {
                val lat = parts[1].toDoubleOrNull()
                val lon = parts[2].toDoubleOrNull()
                if (lat != null && lon != null) add(Place(URLDecoder.decode(parts[0], "UTF-8"), lat, lon))
            }
        }
    }

    fun save(place: Place) {
        val existing = list().filterNot { it.name.equals(place.name, ignoreCase = true) }
        val next = (listOf(place) + existing).take(MAX)
        prefs.edit().apply {
            repeat(MAX) { remove(key(it)) }
            next.forEachIndexed { index, item ->
                putString(key(index), "${URLEncoder.encode(item.name, "UTF-8")}|${item.latitude}|${item.longitude}")
            }
            apply()
        }
    }

    fun remove(name: String) {
        val next = list().filterNot { it.name == name }
        prefs.edit().apply {
            repeat(MAX) { remove(key(it)) }
            next.forEachIndexed { index, item ->
                putString(key(index), "${URLEncoder.encode(item.name, "UTF-8")}|${item.latitude}|${item.longitude}")
            }
            apply()
        }
    }

    companion object {
        private const val FILE = "veyra_saved_places"
        private const val MAX = 12
        private fun key(index: Int) = "place_$index"
    }
}

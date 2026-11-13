package com.HIVFactsheet.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** A nearby health facility returned by the finder. */
data class Clinic(
    val name: String,
    val kind: String,
    val lat: Double,
    val lon: Double,
    val distanceKm: Double,
)

/**
 * Finds nearby HIV care / health facilities with no API key: device location
 * via LocationManager + OpenStreetMap's free Overpass API.
 */
object ClinicFinder {

    fun hasLocationPermission(ctx: Context): Boolean =
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @Suppress("DEPRECATION")
    fun isOnline(ctx: Context): Boolean {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return cm?.activeNetworkInfo?.isConnected == true
    }

    @SuppressLint("MissingPermission")
    fun lastLocation(ctx: Context): Location? {
        if (!hasLocationPermission(ctx)) return null
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        return providers.mapNotNull { p -> runCatching { lm.getLastKnownLocation(p) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    /** Query Overpass for health facilities around the point; sorted by distance. */
    suspend fun nearby(lat: Double, lon: Double, radiusM: Int = 15000): List<Clinic> =
        withContext(Dispatchers.IO) {
            val q = """
                [out:json][timeout:25];
                (
                  node["amenity"~"clinic|hospital|doctors"](around:$radiusM,$lat,$lon);
                  way["amenity"~"clinic|hospital|doctors"](around:$radiusM,$lat,$lon);
                  node["healthcare"](around:$radiusM,$lat,$lon);
                  way["healthcare"](around:$radiusM,$lat,$lon);
                );
                out center 60;
            """.trimIndent()
            val url = URL("https://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(q, "UTF-8"))
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000; readTimeout = 25000
                setRequestProperty("User-Agent", "FactSheet-Android")
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val elements = JSONObject(body).getJSONArray("elements")
            val out = ArrayList<Clinic>()
            val seen = HashSet<String>()
            for (i in 0 until elements.length()) {
                val e = elements.getJSONObject(i)
                val tags = e.optJSONObject("tags") ?: continue
                val name = tags.optString("name").takeIf { it.isNotBlank() } ?: continue
                val plat: Double; val plon: Double
                if (e.has("lat")) { plat = e.getDouble("lat"); plon = e.getDouble("lon") }
                else { val c = e.optJSONObject("center") ?: continue; plat = c.getDouble("lat"); plon = c.getDouble("lon") }
                if (!seen.add(name.lowercase())) continue
                val kind = tags.optString("healthcare").ifBlank { tags.optString("amenity") }
                out += Clinic(name, kind.replaceFirstChar { it.uppercase() }, plat, plon, haversineKm(lat, lon, plat, plon))
            }
            out.sortedBy { it.distanceKm }.take(25)
        }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}

package com.HIVFactsheet.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
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

    private const val TAG = "ClinicFinder"

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

    private val MIRRORS = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter",
        "https://maps.mail.ru/osm/tools/overpass/api/interpreter",
    )

    /** Query Overpass (POST, with mirror fallback) for nearby facilities. */
    suspend fun nearby(lat: Double, lon: Double, radiusM: Int = 15000): List<Clinic> =
        withContext(Dispatchers.IO) {
            val q = "[out:json][timeout:20];(" +
                "node[\"amenity\"~\"clinic|hospital|doctors\"](around:$radiusM,$lat,$lon);" +
                "way[\"amenity\"~\"clinic|hospital|doctors\"](around:$radiusM,$lat,$lon);" +
                "node[\"healthcare\"](around:$radiusM,$lat,$lon);" +
                "way[\"healthcare\"](around:$radiusM,$lat,$lon);" +
                ");out center 60;"
            val payload = ("data=" + URLEncoder.encode(q, "UTF-8")).toByteArray()
            var lastErr: Exception? = null
            for (endpoint in MIRRORS) {
                try {
                    val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"; doOutput = true
                        connectTimeout = 6000; readTimeout = 9000
                        setRequestProperty("User-Agent", "FactSheet-Android")
                        setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    }
                    conn.outputStream.use { it.write(payload) }
                    val code = conn.responseCode
                    if (code !in 200..299) {
                        lastErr = IOException("HTTP $code from $endpoint")
                        Log.w(TAG, "overpass $endpoint -> HTTP $code")
                        conn.disconnect(); continue
                    }
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    return@withContext parse(body, lat, lon)
                } catch (e: Exception) {
                    lastErr = e
                    Log.w(TAG, "overpass $endpoint failed: ${e.javaClass.simpleName}: ${e.message}")
                }
            }
            throw lastErr ?: IOException("No clinic data")
        }

    private fun parse(body: String, lat: Double, lon: Double): List<Clinic> {
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
        return out.sortedBy { it.distanceKm }.take(25)
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

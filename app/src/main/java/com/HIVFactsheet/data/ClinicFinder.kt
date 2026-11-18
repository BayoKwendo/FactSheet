package com.HIVFactsheet.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.util.Log
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
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

    // Curated, bundled list of Kenyan HIV care facilities so the list always
    // works offline / when Overpass is down. Loaded once and cached.
    private var bundled: List<Clinic>? = null

    private fun bundledClinics(ctx: Context): List<Clinic> = bundled ?: run {
        val list = runCatching {
            val txt = ctx.assets.open("clinics.json").use { String(it.readBytes(), Charsets.UTF_8) }
            val arr = org.json.JSONArray(txt)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Clinic(o.getString("name"), o.optString("kind"), o.getDouble("lat"), o.getDouble("lon"), 0.0)
            }
        }.getOrDefault(emptyList())
        list.also { bundled = it }
    }

    /** Nearest bundled facilities to a point — always available, no network. */
    fun offlineNearby(ctx: Context, lat: Double, lon: Double, limit: Int = 15): List<Clinic> =
        bundledClinics(ctx)
            .map { it.copy(distanceKm = haversineKm(lat, lon, it.lat, it.lon)) }
            .sortedBy { it.distanceKm }
            .take(limit)

    /** Whether location services (GPS/network) are switched on, device-wide. */
    fun isLocationEnabled(ctx: Context): Boolean {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return LocationManagerCompat.isLocationEnabled(lm)
    }

    @SuppressLint("MissingPermission")
    fun lastLocation(ctx: Context): Location? {
        if (!hasLocationPermission(ctx)) return null
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        return providers.mapNotNull { p -> runCatching { lm.getLastKnownLocation(p) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    /**
     * Best available location: a recent cached fix if we have one, otherwise an
     * actively-requested fresh fix (works even when no app has used GPS lately).
     * Returns null only if permission is missing, location is off, or it times out.
     */
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(ctx: Context): Location? {
        if (!hasLocationPermission(ctx)) return null
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        // Fast path: a cached fix less than two minutes old is good enough.
        lastLocation(ctx)?.let { if (System.currentTimeMillis() - it.time < 120_000) return it }
        val provider = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return lastLocation(ctx)
        }
        val fresh = withTimeoutOrNull(12_000) {
            suspendCancellableCoroutine<Location?> { cont ->
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                LocationManagerCompat.getCurrentLocation(
                    lm, provider, signal, ContextCompat.getMainExecutor(ctx),
                ) { loc -> if (cont.isActive) cont.resume(loc) }
            }
        }
        return fresh ?: lastLocation(ctx)
    }

    // Public Overpass instances (global coverage). The free servers are often
    // overloaded (HTTP 429/502/504) or slow, so we try several and retry once.
    // NOTE: region-specific instances (e.g. overpass.osm.ch, Switzerland only)
    // must NOT be added — they return 0 results elsewhere and look like "empty".
    private val MIRRORS = listOf(
        "https://overpass-api.de/api/interpreter",
        "https://overpass.private.coffee/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter",
        "https://maps.mail.ru/osm/tools/overpass/api/interpreter",
    )

    private const val MAX_PASSES = 2
    private const val CONNECT_MS = 7000
    private const val READ_MS = 15000
    // Total wall-clock budget so the in-app list fails fast (and defers to the
    // Maps button) instead of hanging when every mirror is down.
    private const val DEADLINE_MS = 35000L

    /** Query Overpass (POST, mirror fallback + one retry, bounded by a deadline). */
    suspend fun nearby(lat: Double, lon: Double, radiusM: Int = 15000): List<Clinic> =
        withContext(Dispatchers.IO) {
            // Keep the server-side timeout in step with our socket read timeout.
            val q = "[out:json][timeout:20];(" +
                "node[\"amenity\"~\"clinic|hospital|doctors\"](around:$radiusM,$lat,$lon);" +
                "way[\"amenity\"~\"clinic|hospital|doctors\"](around:$radiusM,$lat,$lon);" +
                "node[\"healthcare\"](around:$radiusM,$lat,$lon);" +
                "way[\"healthcare\"](around:$radiusM,$lat,$lon);" +
                ");out center 60;"
            val payload = ("data=" + URLEncoder.encode(q, "UTF-8")).toByteArray()
            val deadline = System.currentTimeMillis() + DEADLINE_MS
            var lastErr: Exception? = null
            repeat(MAX_PASSES) { pass ->
                for (endpoint in MIRRORS) {
                    if (System.currentTimeMillis() >= deadline) {
                        Log.w(TAG, "overpass deadline reached, giving up")
                        return@repeat
                    }
                    try {
                        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                            requestMethod = "POST"; doOutput = true
                            // A too-short timeout (9s) was the main cause of the old
                            // "unavailable" message; Overpass often needs 10-15s.
                            connectTimeout = CONNECT_MS; readTimeout = READ_MS
                            // Overpass wants a descriptive User-Agent; anonymous
                            // requests are more likely to be throttled.
                            setRequestProperty("User-Agent", "FactSheet-Android (HIV clinic finder)")
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
                // Brief backoff before retrying the whole list once more.
                if (pass < MAX_PASSES - 1 && System.currentTimeMillis() < deadline) delay(1200)
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

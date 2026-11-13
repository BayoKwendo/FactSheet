package com.HIVFactsheet.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.HIVFactsheet.data.Clinic
import com.HIVFactsheet.data.ClinicFinder

private sealed interface FinderState {
    data object Loading : FinderState
    data object NeedPermission : FinderState
    data object Offline : FinderState
    data object NoLocation : FinderState
    data object Empty : FinderState
    data class Error(val msg: String) : FinderState
    data class Results(val list: List<Clinic>) : FinderState
}

/**
 * Find-a-Health-Center tab: when online + permitted, auto-queries nearby HIV
 * care / health facilities and lists them; each opens in maps for directions.
 */
@Composable
fun HealthCenterScreen() {
    val ctx = LocalContext.current
    var hasPerm by remember { mutableStateOf(ClinicFinder.hasLocationPermission(ctx)) }
    var reload by remember { mutableStateOf(0) }
    var state by remember { mutableStateOf<FinderState>(FinderState.Loading) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPerm = result.values.any { it }
        reload++
    }

    // Auto-request location once if we don't have it yet.
    LaunchedEffect(Unit) {
        if (!hasPerm) launcher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
    }

    // Auto-query whenever permission/connectivity/reload changes.
    LaunchedEffect(hasPerm, reload) {
        state = FinderState.Loading
        when {
            !hasPerm -> state = FinderState.NeedPermission
            !ClinicFinder.isOnline(ctx) -> state = FinderState.Offline
            else -> {
                val loc = ClinicFinder.lastLocation(ctx)
                state = if (loc == null) FinderState.NoLocation
                else try {
                    val list = ClinicFinder.nearby(loc.latitude, loc.longitude)
                    if (list.isEmpty()) FinderState.Empty else FinderState.Results(list)
                } catch (e: Exception) {
                    FinderState.Error("Couldn't load clinics. Check your connection and try again.")
                }
            }
        }
    }

    fun openMapsSearch() {
        val q = Uri.encode("Comprehensive Care Centre CCC HIV clinic")
        runCatching {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$q")))
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "HIV care & health facilities near you",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(12.dp))

        // Primary, always-reliable action: open maps to nearby CCCs.
        MapsButton(onClick = { openMapsSearch() })
        Spacer(Modifier.height(16.dp))

        // Best-effort in-app list below (may be unavailable on slow networks).
        when (val s = state) {
            is FinderState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(color = BrandPurple, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Finding clinics in the app…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            is FinderState.NeedPermission -> InlineNote(
                "Allow location to list nearby facilities here.",
                "Allow location",
            ) {
                launcher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                    )
                )
            }
            is FinderState.Offline -> InlineNote("You're offline — use the button above when connected.", "Retry") { reload++ }
            is FinderState.NoLocation -> InlineNote("Turn on location to list facilities here.", "Retry") { reload++ }
            is FinderState.Empty -> InlineNote("No in-app results found — use the button above.", "Retry") { reload++ }
            is FinderState.Error -> InlineNote("In-app list unavailable right now — use the button above.", "Retry") { reload++ }
            is FinderState.Results -> {
                Text(
                    "Nearby facilities • tap for directions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(s.list) { c -> ClinicCard(c) { openClinic(ctx, c) } }
                }
            }
        }
    }
}

@Composable
private fun MapsButton(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BrandPurple, contentColor = BrandCyan),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = BrandCyan)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Find clinics near me", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = BrandCyan)
                Text("Opens Google Maps at HIV care centres near you", fontSize = 12.sp, color = BrandCyan.copy(alpha = 0.85f))
            }
        }
    }
}

@Composable
private fun InlineNote(text: String, action: String, onAction: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.weight(1f))
        androidx.compose.material3.TextButton(onClick = onAction) { Text(action, color = BrandPurple) }
    }
}

private fun openClinic(ctx: android.content.Context, c: Clinic) {
    val uri = Uri.parse("geo:${c.lat},${c.lon}?q=${c.lat},${c.lon}(${Uri.encode(c.name)})")
    runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
        .onFailure {
            val q = Uri.encode(c.name)
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$q")))
        }
}

@Composable
private fun ClinicCard(c: Clinic, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = BrandPurple)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(c.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    buildString {
                        if (c.kind.isNotBlank()) append(c.kind).append(" • ")
                        append(if (c.distanceKm < 1) "${(c.distanceKm * 1000).toInt()} m away" else "%.1f km away".format(c.distanceKm))
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun Message(
    text: String,
    action: String,
    onAction: () -> Unit,
    secondary: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text(text, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = BrandCyan),
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null); Spacer(Modifier.width(6.dp)); Text(action)
            }
            if (secondary != null && onSecondary != null) {
                androidx.compose.material3.TextButton(onClick = onSecondary) {
                    Text(secondary, color = BrandPurple)
                }
            }
        }
    }
}

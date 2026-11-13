package com.HIVFactsheet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import com.HIVFactsheet.data.Repository

/** Full-text search across every topic's title and content. */
@Composable
fun SearchScreen(onOpen: (String) -> Unit, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = remember(query) { Repository.search(query) }

    AppScaffold(title = "Search", onBack = onBack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                placeholder = { Text("Search all topics…") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth().padding(12.dp),
            )

            if (query.length >= 2 && results.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("No topics match “$query”.", color = BrandPurple)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(results) { hit ->
                    Card(
                        onClick = { onOpen(hit.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = androidx.compose.ui.graphics.Color.White,
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                hit.title,
                                color = BrandPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                            if (hit.snippet.isNotBlank()) {
                                Text(
                                    hit.snippet,
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    color = androidx.compose.ui.graphics.Color(0xFF444444),
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

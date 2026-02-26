package com.example.langfire_app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.langfire_app.presentation.ui.theme.FireOrange
import com.example.langfire_app.presentation.ui.theme.SurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Verbs", "Nouns", "Adjectives", "Phrases", "Favorites")
    var selectedCategory by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // --- Header Section ---
        PaddingValues(horizontal = 20.dp, vertical = 16.dp).let {
            Column(modifier = Modifier.padding(it)) {
                Text(
                    text = "My Library",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "You've mastered 112 words so far!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // --- Search Bar ---
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = {},
            active = false,
            onActiveChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            placeholder = { Text("Search your vocabulary...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = null) },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {}

        Spacer(modifier = Modifier.height(24.dp))

        // --- Categories Chips ---
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Word List ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mockWords) { wordItem ->
                WordCard(wordItem)
            }
        }
    }
}

@Composable
fun WordCard(wordItem: MockWord) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = wordItem.original,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { /* Play Audio */ },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Outlined.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = wordItem.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Progress Bar
                LinearProgressIndicator(
                    progress = wordItem.mastery,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = FireOrange,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            
            // Level Badge
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = wordItem.level,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

data class MockWord(
    val original: String,
    val translation: String,
    val level: String,
    val mastery: Float
)

val mockWords = listOf(
    MockWord("Gezellig", "Cozy, companionable", "A2", 0.8f),
    MockWord("Eekhoorn", "Squirrel", "A1", 1.0f),
    MockWord("Vliegtuig", "Airplane", "A1", 0.4f),
    MockWord("Praten", "To talk", "A1", 0.9f),
    MockWord("Lopen", "To walk", "A1", 0.7f),
    MockWord("Drinken", "To drink", "A1", 0.3f),
    MockWord("Eten", "To eat", "A1", 1.0f)
)

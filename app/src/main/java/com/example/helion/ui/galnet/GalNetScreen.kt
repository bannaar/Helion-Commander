package com.example.helion.ui.galnet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.helion.core.model.GalNetArticle
import com.example.helion.core.model.GalNetChannel
import com.example.helion.ui.components.HelionCard
import com.example.ui.theme.HelionAmber
import com.example.ui.theme.HelionBorder
import com.example.ui.theme.HelionCyan
import com.example.ui.theme.HelionDangerRed
import com.example.ui.theme.HelionDeepGraphite
import com.example.ui.theme.HelionHighSecGreen
import com.example.ui.theme.HelionSurface
import com.example.ui.theme.HelionSurfaceHigh
import com.example.ui.theme.HelionSurfaceVariant
import com.example.ui.theme.HelionTextMuted
import com.example.ui.theme.HelionTextPrimary
import com.example.ui.theme.HelionTextSecondary

@Composable
fun GalNetScreen(
    viewModel: GalNetViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = HelionCyan)
        }
        return
    }

    if (state.selectedArticle != null) {
        ArticleDetailView(
            article = state.selectedArticle!!,
            onBack = { viewModel.closeArticleDetail() },
            onToggleSave = { viewModel.toggleSaveArticle(it) },
            modifier = modifier
        )
    } else {
        ArticleListContent(
            state = state,
            onSelectChannel = { viewModel.selectChannel(it) },
            onToggleSaved = { viewModel.toggleSavedFilter() },
            onSearchQuery = { viewModel.setSearchQuery(it) },
            onSelectArticle = { viewModel.selectArticle(it) },
            modifier = modifier
        )
    }
}

@Composable
fun ArticleListContent(
    state: GalNetUiState,
    onSelectChannel: (GalNetChannel?) -> Unit,
    onToggleSaved: () -> Unit,
    onSearchQuery: (String) -> Unit,
    onSelectArticle: (GalNetArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    val filtered = state.articles.filter { art ->
        val matchesChannel = if (state.showSavedOnly) {
            art.isSaved
        } else if (state.selectedChannel != null) {
            art.category == state.selectedChannel
        } else true

        val matchesSearch = art.headline.contains(state.searchQuery, ignoreCase = true) ||
                art.summary.contains(state.searchQuery, ignoreCase = true)

        matchesChannel && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("galnet_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Channel Filter Bar
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = state.selectedChannel == null && !state.showSavedOnly,
                        onClick = { onSelectChannel(null) },
                        label = { Text("Top Stories") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HelionCyan,
                            selectedLabelColor = HelionDeepGraphite
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = state.showSavedOnly,
                        onClick = onToggleSaved,
                        label = { Text("Saved Stories") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HelionAmber,
                            selectedLabelColor = HelionDeepGraphite
                        )
                    )
                }
                items(GalNetChannel.values()) { ch ->
                    FilterChip(
                        selected = state.selectedChannel == ch && !state.showSavedOnly,
                        onClick = { onSelectChannel(ch) },
                        label = { Text(ch.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HelionCyan,
                            selectedLabelColor = HelionDeepGraphite
                        )
                    )
                }
            }
        }

        // Search Field
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQuery,
                label = { Text("Filter GalNet news feed...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = HelionCyan) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HelionCyan,
                    unfocusedBorderColor = HelionBorder
                )
            )
        }

        items(filtered) { article ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectArticle(article) },
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = HelionSurface),
                border = BorderStroke(1.dp, HelionBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!article.isRead) {
                                Icon(
                                    imageVector = Icons.Default.FiberManualRecord,
                                    contentDescription = "Unread",
                                    tint = HelionCyan,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = if (article.importance == "FLASH" || article.importance == "CRITICAL") HelionDangerRed.copy(alpha = 0.2f) else HelionSurfaceHigh,
                                border = if (article.importance == "FLASH" || article.importance == "CRITICAL") BorderStroke(0.8.dp, HelionDangerRed) else null
                            ) {
                                Text(
                                    text = article.category.label.uppercase(),
                                    color = if (article.importance == "FLASH" || article.importance == "CRITICAL") HelionDangerRed else HelionCyan,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (article.isSaved) {
                            Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Saved", tint = HelionAmber, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.headline,
                        style = MaterialTheme.typography.titleMedium,
                        color = HelionTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = article.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = HelionTextSecondary,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
fun ArticleDetailView(
    article: GalNetArticle,
    onBack: () -> Unit,
    onToggleSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HelionCyan)
            }
            IconButton(onClick = { onToggleSave(article.articleId) }) {
                Icon(
                    imageVector = if (article.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save Article",
                    tint = if (article.isSaved) HelionAmber else HelionTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(4.dp),
            color = HelionSurfaceHigh
        ) {
            Text(
                text = "${article.category.label.uppercase()} • WIRE BULLETIN",
                style = MaterialTheme.typography.labelSmall,
                color = HelionCyan,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = article.headline,
            style = MaterialTheme.typography.headlineMedium,
            color = HelionTextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = HelionBorder)
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = article.body,
            style = MaterialTheme.typography.bodyLarge,
            color = HelionTextPrimary,
            lineHeight = 22.sp
        )
    }
}

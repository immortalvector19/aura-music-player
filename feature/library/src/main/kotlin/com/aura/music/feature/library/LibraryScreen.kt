package com.aura.music.feature.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aura.music.core.designsystem.theme.SpotifyBlack
import com.aura.music.core.designsystem.theme.SpotifyDarkest
import com.aura.music.core.designsystem.theme.SpotifyGreen
import com.aura.music.core.designsystem.theme.SpotifyGreenBright
import com.aura.music.core.designsystem.theme.SpotifyMuted
import com.aura.music.core.designsystem.theme.SpotifySubdued
import com.aura.music.core.designsystem.theme.SpotifySurface
import com.aura.music.core.designsystem.theme.SpotifySurfaceElevated
import com.aura.music.core.designsystem.theme.SpotifySurfaceHighlight
import com.aura.music.core.designsystem.theme.SpotifyWhite
import com.aura.music.core.model.Album
import com.aura.music.core.model.Artist
import com.aura.music.core.model.StorageInfo
import com.aura.music.core.model.Track
import com.aura.music.core.ui.TrackListItem

@Composable
fun LibraryRoute(
    viewModel: LibraryViewModel,
    onTrackClick: (Track) -> Unit,
    currentTrackId: String? = null,
    modifier: Modifier = Modifier,
    onAlbumClick: (Album) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {}
) {
    LibraryScreen(
        viewModel = viewModel,
        onTrackClick = onTrackClick,
        currentTrackId = currentTrackId,
        onAlbumClick = onAlbumClick,
        onArtistClick = onArtistClick,
        modifier = modifier
    )
}

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onTrackClick: (Track) -> Unit,
    currentTrackId: String? = null,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()

    Scaffold(
        containerColor = SpotifyBlack,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SpotifyBlack)
                    .padding(top = 8.dp)
            ) {
                // Header Title + Quick Action
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Your Library",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = SpotifyWhite
                    )

                    IconButton(
                        onClick = viewModel::onScanLibrary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rescan Library",
                            tint = SpotifyMuted
                        )
                    }
                }

                // Spotify Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = "Find in library",
                            fontSize = 14.sp,
                            color = SpotifyMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = SpotifyMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = SpotifyMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SpotifySurfaceHighlight,
                        unfocusedContainerColor = SpotifySurfaceElevated,
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = SpotifyWhite,
                        unfocusedTextColor = SpotifyWhite,
                        cursorColor = SpotifyGreen
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .height(50.dp)
                )

                // Category Filter Pills (Spotify Chips)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = LibraryTab.values()
                    tabs.forEach { tab ->
                        val isSelected = uiState.selectedTab == tab
                        SpotifyFilterChip(
                            text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                            isSelected = isSelected,
                            onClick = { viewModel.onTabSelected(tab) }
                        )
                    }
                }

                // Scanning Progress Indicator
                AnimatedVisibility(visible = uiState.isScanning) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = SpotifyGreenBright,
                        trackColor = SpotifySurfaceHighlight
                    )
                }

                // Low Storage Warning Banner
                val hasLowStorage = uiState.storageLocations.any { it.isLowStorage }
                AnimatedVisibility(visible = hasLowStorage) {
                    Surface(
                        color = Color(0xFF3E1216),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFFDAD6)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Storage critically low (<500MB). Caching and downloads paused.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFFDAD6)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SpotifyBlack)
        ) {
            when (uiState.selectedTab) {
                LibraryTab.TRACKS -> {
                    if (tracks.isEmpty() && !uiState.isScanning) {
                        EmptyLibraryView(onScan = viewModel::onScanLibrary)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 96.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(tracks, key = { it.id }) { track ->
                                TrackListItem(
                                    track = track,
                                    isPlaying = (currentTrackId == track.id.toString()),
                                    onClick = { onTrackClick(track) },
                                    onToggleFavorite = { isFav -> viewModel.onToggleFavorite(track.id, isFav) }
                                )
                            }
                        }
                    }
                }

                LibraryTab.ALBUMS -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(albums, key = { it.id }) { album ->
                            SpotifyAlbumCard(album = album, onClick = { onAlbumClick(album) })
                        }
                    }
                }

                LibraryTab.ARTISTS -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 96.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(artists, key = { it.id }) { artist ->
                            SpotifyArtistItem(artist = artist, onClick = { onArtistClick(artist) })
                        }
                    }
                }

                LibraryTab.STORAGE -> {
                    StorageLocationsView(locations = uiState.storageLocations)
                }
            }
        }
    }
}

@Composable
private fun SpotifyFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) SpotifyGreen else SpotifySurfaceHighlight,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) SpotifyDarkest else SpotifyWhite,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun SpotifyAlbumCard(album: Album, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(SpotifySurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            if (!album.albumArtUri.isNullOrEmpty()) {
                AsyncImage(
                    model = album.albumArtUri,
                    contentDescription = "Cover for ${album.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    tint = SpotifySubdued,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = album.title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = SpotifyWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Album • ${album.artist}",
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = SpotifyMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SpotifyArtistItem(artist: Artist, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = SpotifySurfaceElevated,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = SpotifyMuted,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = SpotifyWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Artist • ${artist.trackCount} songs",
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = SpotifyMuted
            )
        }
    }
}

@Composable
private fun StorageLocationsView(locations: List<StorageInfo>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Storage Volumes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyWhite,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        items(locations) { info ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = SpotifySurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (info.isExternalSd) Icons.Default.SdCard else Icons.Default.Storage,
                        contentDescription = null,
                        tint = if (info.isLowStorage) Color(0xFFE91429) else SpotifyGreen,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (info.isExternalSd) "Removable SD Card" else "Internal Device Storage",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SpotifyWhite
                        )
                        val freeGb = info.availableBytes / (1024 * 1024 * 1024.0)
                        val totalGb = info.totalBytes / (1024 * 1024 * 1024.0)
                        Text(
                            text = String.format("%.1f GB free of %.1f GB", freeGb, totalGb),
                            fontSize = 13.sp,
                            color = if (info.isLowStorage) Color(0xFFE91429) else SpotifyMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = info.storagePath,
                            fontSize = 11.sp,
                            color = SpotifySubdued,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryView(onScan: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = SpotifySubdued,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Your library is empty",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scan your device to discover downloaded audio files and music tracks.",
                fontSize = 14.sp,
                color = SpotifyMuted,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onScan,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SpotifyGreen,
                    contentColor = SpotifyDarkest
                ),
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Scan Device",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

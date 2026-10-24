package com.aura.music.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.aura.music.core.designsystem.theme.SpotifyGreen
import com.aura.music.core.designsystem.theme.SpotifyGreenBright
import com.aura.music.core.designsystem.theme.SpotifyMuted
import com.aura.music.core.designsystem.theme.SpotifySurfaceHighlight
import com.aura.music.core.designsystem.theme.SpotifyWhite
import com.aura.music.core.model.Track

/**
 * Spotify-styled track list item with 48dp artwork, highlighted green active state,
 * secondary metadata hierarchy, and accessible action targets.
 */
@Composable
fun TrackListItem(
    track: Track,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onToggleFavorite: (Boolean) -> Unit,
    onMoreOptionsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isPlaying) SpotifySurfaceHighlight else SpotifyBlack,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            // Album Art Thumbnail
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF242424)),
                contentAlignment = Alignment.Center
            ) {
                if (!track.albumArtUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = track.albumArtUri,
                        contentDescription = "Cover for ${track.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isPlaying) SpotifyGreenBright else SpotifyMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Metadata: Title and Artist with Spotify hierarchy
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 15.sp,
                    color = if (isPlaying) SpotifyGreenBright else SpotifyWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.formattedDuration}",
                    fontSize = 13.sp,
                    color = SpotifyMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Favorite Button
            IconButton(
                onClick = { onToggleFavorite(!track.isFavorite) },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (track.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (track.isFavorite) SpotifyGreen else SpotifyMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            // More Options (3 dots)
            IconButton(
                onClick = onMoreOptionsClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options for ${track.title}",
                    tint = SpotifyMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

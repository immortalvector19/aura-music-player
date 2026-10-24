package com.aura.music.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun PermissionRationaleDialog(
    permissionType: PermissionType,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val (title, body, icon) = when (permissionType) {
        PermissionType.MEDIA_AUDIO -> Triple(
            "Media Library Access",
            "Aura Music needs permission to scan and index your audio files, albums, and playlists from device storage and SD cards.",
            Icons.Default.MusicNote
        )
        PermissionType.NOTIFICATIONS -> Triple(
            "Playback Notification",
            "Aura Music requires notification permission to display background media controls on your lock screen and notification shade.",
            Icons.Default.Notifications
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Allow Access", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Not Now", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

enum class PermissionType {
    MEDIA_AUDIO,
    NOTIFICATIONS
}

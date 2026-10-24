package com.aura.music.core.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aura.music.core.designsystem.theme.SpotifyBlack
import com.aura.music.core.designsystem.theme.SpotifyDivider
import com.aura.music.core.designsystem.theme.SpotifyGreen
import com.aura.music.core.designsystem.theme.SpotifyMuted
import com.aura.music.core.designsystem.theme.SpotifySurfaceElevated
import com.aura.music.core.designsystem.theme.SpotifyWhite
import com.aura.music.core.model.AudioDiagnosticInfo

/**
 * On-device live diagnostic panel for inspecting the real ExoPlayer audio pipeline,
 * ContentResolver file descriptor accessibility, volume levels, and audio session routing.
 */
@Composable
fun AudioDiagnosticsDialog(
    diagnosticInfo: AudioDiagnosticInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val formattedReport = """
        === AURA AUDIO DIAGNOSTICS REPORT ===
        Player State: ${diagnosticInfo.playerState}
        isPlaying: ${diagnosticInfo.isPlaying}
        playWhenReady: ${diagnosticInfo.playWhenReady}
        Player Volume: ${diagnosticInfo.playerVolume}
        Device Volume (STREAM_MUSIC): ${diagnosticInfo.deviceStreamVolume}
        AudioSessionId: ${diagnosticInfo.audioSessionId}
        Equalizer Bypassed: ${diagnosticInfo.equalizerBypassed}
        Media URI: ${diagnosticInfo.currentMediaUri}
        ContentResolver Status: ${diagnosticInfo.contentResolverStatus}
        Last Error: ${diagnosticInfo.lastError ?: "None"}
        =====================================
    """.trimIndent()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SpotifySurfaceElevated,
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, SpotifyDivider, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Audio Pipeline Audit",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyWhite
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SpotifyMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Diagnostic Items Table
                DiagnosticRow(label = "ExoPlayer State", value = diagnosticInfo.playerState, isEmphasized = true)
                DiagnosticRow(label = "Is Playing (Active)", value = diagnosticInfo.isPlaying.toString())
                DiagnosticRow(label = "playWhenReady", value = diagnosticInfo.playWhenReady.toString())
                DiagnosticRow(label = "Player Volume", value = "${(diagnosticInfo.playerVolume * 100).toInt()}% (${diagnosticInfo.playerVolume})")
                DiagnosticRow(label = "Device Media Volume", value = diagnosticInfo.deviceStreamVolume)
                DiagnosticRow(label = "AudioSessionId", value = diagnosticInfo.audioSessionId.toString())
                DiagnosticRow(
                    label = "Equalizer Engine",
                    value = if (diagnosticInfo.equalizerBypassed) "BYPASSED (Disabled)" else "ATTACHED & ACTIVE"
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Track URI & ContentResolver Access",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpotifyMuted
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SpotifyBlack)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = if (diagnosticInfo.currentMediaUri.isNotBlank()) diagnosticInfo.currentMediaUri else "No active track URI",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = SpotifyWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isOk = diagnosticInfo.contentResolverStatus.startsWith("SUCCESS")
                            Icon(
                                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isOk) SpotifyGreen else Color(0xFFFF5252),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = diagnosticInfo.contentResolverStatus,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isOk) SpotifyGreen else Color(0xFFFF5252)
                            )
                        }
                    }
                }

                if (diagnosticInfo.lastError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Last Error: ${diagnosticInfo.lastError}",
                        fontSize = 11.sp,
                        color = Color(0xFFFF5252)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Aura Audio Diagnostics", formattedReport)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Diagnostics copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SpotifyWhite)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Copy Logs", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen, contentColor = SpotifyBlack)
                    ) {
                        Text(text = "Dismiss", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
    isEmphasized: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = SpotifyMuted)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isEmphasized) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = if (isEmphasized) SpotifyGreen else SpotifyWhite
        )
    }
}

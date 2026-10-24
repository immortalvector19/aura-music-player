package com.aura.music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.music.core.data.repository.PreferencesRepository
import com.aura.music.core.data.repository.TrackRepository
import com.aura.music.core.data.storage.StorageManager
import com.aura.music.core.designsystem.theme.AuraTheme
import com.aura.music.core.designsystem.theme.SpotifyBlack
import com.aura.music.core.designsystem.theme.SpotifyDarkest
import com.aura.music.core.designsystem.theme.SpotifyDivider
import com.aura.music.core.designsystem.theme.SpotifyMuted
import com.aura.music.core.designsystem.theme.SpotifyWhite
import com.aura.music.core.playback.EqualizerEngine
import com.aura.music.core.playback.PlaybackManager
import com.aura.music.core.ui.MiniPlayerBar
import com.aura.music.core.ui.PermissionRationaleDialog
import com.aura.music.core.ui.PermissionType
import com.aura.music.feature.equalizer.EqualizerRoute
import com.aura.music.feature.equalizer.EqualizerViewModel
import com.aura.music.feature.library.LibraryRoute
import com.aura.music.feature.library.LibraryViewModel
import com.aura.music.feature.player.PlayerSheet
import com.aura.music.feature.player.PlayerViewModel
import com.aura.music.feature.settings.SettingsRoute
import com.aura.music.feature.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppDestination(val title: String, val icon: ImageVector) {
    LIBRARY("Library", Icons.Default.LibraryMusic),
    EQUALIZER("Equalizer", Icons.Default.GraphicEq),
    SETTINGS("Settings", Icons.Default.Settings)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var trackRepository: TrackRepository

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var storageManager: StorageManager

    @Inject
    lateinit var equalizerEngine: EqualizerEngine

    @Inject
    lateinit var playbackManager: PlaybackManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AuraTheme {
                val coroutineScope = rememberCoroutineScope()
                var currentDestination by remember { mutableStateOf(AppDestination.LIBRARY) }
                var isPlayerSheetExpanded by remember { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                // ViewModels
                val libraryViewModel = remember { LibraryViewModel(trackRepository) }
                val playerViewModel = remember { PlayerViewModel(playbackManager) }
                val equalizerViewModel = remember { EqualizerViewModel(equalizerEngine) }
                val settingsViewModel = remember { SettingsViewModel(preferencesRepository, storageManager) }

                val currentTrack by playerViewModel.currentTrack.collectAsStateWithLifecycle()
                val playbackState by playerViewModel.playbackState.collectAsStateWithLifecycle()

                // Permission Handling
                var showAudioPermissionRationale by remember { mutableStateOf(false) }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val audioGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions[Manifest.permission.READ_MEDIA_AUDIO] == true
                    } else {
                        permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                    }
                    if (audioGranted) {
                        libraryViewModel.onScanLibrary()
                    }
                }

                LaunchedEffect(Unit) {
                    val permissionsToRequest = mutableListOf<String>()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
                        }
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }

                    if (permissionsToRequest.isNotEmpty()) {
                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                    } else {
                        libraryViewModel.onScanLibrary()
                    }
                }

                Scaffold(
                    containerColor = SpotifyBlack,
                    bottomBar = {
                        Column {
                            // Persistent MiniPlayerBar anchored above NavigationBar
                            AnimatedVisibility(
                                visible = currentTrack != null,
                                enter = slideInVertically { it },
                                exit = slideOutVertically { it }
                            ) {
                                val progress = if ((currentTrack?.durationMs ?: 0L) > 0) {
                                    playbackState.currentPositionMs.toFloat() / currentTrack!!.durationMs.toFloat()
                                } else 0f

                                MiniPlayerBar(
                                    track = currentTrack,
                                    isPlaying = playbackState.isPlaying,
                                    progress = progress,
                                    onPlayPauseClick = playerViewModel::togglePlayPause,
                                    onSkipNextClick = playerViewModel::skipNext,
                                    onClick = { isPlayerSheetExpanded = true }
                                )
                            }

                            // Spotify Dark Minimalist Navigation Bar
                            NavigationBar(
                                containerColor = SpotifyDarkest,
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 0.5.dp,
                                        color = SpotifyDivider
                                    )
                            ) {
                                AppDestination.entries.forEach { destination ->
                                    val isSelected = currentDestination == destination
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentDestination = destination },
                                        icon = {
                                            Icon(
                                                imageVector = destination.icon,
                                                contentDescription = destination.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = destination.title,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = SpotifyWhite,
                                            selectedTextColor = SpotifyWhite,
                                            unselectedIconColor = SpotifyMuted,
                                            unselectedTextColor = SpotifyMuted,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentDestination) {
                            AppDestination.LIBRARY -> {
                                LibraryRoute(
                                    viewModel = libraryViewModel,
                                    currentTrackId = currentTrack?.id?.toString(),
                                    onTrackClick = { track ->
                                        val trackList = libraryViewModel.tracks.value
                                        val index = trackList.indexOfFirst { it.id == track.id }
                                        if (index >= 0) {
                                            playerViewModel.setQueue(trackList, index)
                                        } else {
                                            playerViewModel.playTrack(track)
                                        }
                                    }
                                )
                            }
                            AppDestination.EQUALIZER -> {
                                EqualizerRoute(
                                    viewModel = equalizerViewModel,
                                    onBackClick = { currentDestination = AppDestination.LIBRARY }
                                )
                            }
                            AppDestination.SETTINGS -> {
                                SettingsRoute(
                                    viewModel = settingsViewModel,
                                    onBackClick = { currentDestination = AppDestination.LIBRARY }
                                )
                            }
                        }
                    }

                    // Full-screen Player Sheet Modal
                    if (isPlayerSheetExpanded && currentTrack != null) {
                        ModalBottomSheet(
                            onDismissRequest = { isPlayerSheetExpanded = false },
                            sheetState = sheetState,
                            containerColor = SpotifyBlack,
                            dragHandle = null
                        ) {
                            PlayerSheet(
                                track = currentTrack,
                                playbackState = playbackState,
                                onPlayPauseClick = playerViewModel::togglePlayPause,
                                onSkipNextClick = playerViewModel::skipNext,
                                onSkipPreviousClick = playerViewModel::skipPrevious,
                                onSeekTo = playerViewModel::seekTo,
                                onToggleRepeat = playerViewModel::toggleRepeat,
                                onToggleShuffle = playerViewModel::toggleShuffle,
                                onToggleFavorite = playerViewModel::toggleFavorite,
                                onDismiss = {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        isPlayerSheetExpanded = false
                                    }
                                },
                                onOpenQueue = {}
                            )
                        }
                    }

                    // Permission Rationale Dialog
                    if (showAudioPermissionRationale) {
                        PermissionRationaleDialog(
                            permissionType = PermissionType.MEDIA_AUDIO,
                            onConfirm = {
                                showAudioPermissionRationale = false
                                val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Manifest.permission.READ_MEDIA_AUDIO
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                                permissionLauncher.launch(arrayOf(perm))
                            },
                            onDismiss = { showAudioPermissionRationale = false }
                        )
                    }
                }
            }
        }
    }
}

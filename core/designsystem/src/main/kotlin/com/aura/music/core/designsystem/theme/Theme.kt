package com.aura.music.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Spotify-standard dark color scheme
private val SpotifyDarkColorScheme = darkColorScheme(
    primary = SpotifyGreenBright,
    onPrimary = SpotifyDarkest,
    primaryContainer = SpotifyGreenDark,
    onPrimaryContainer = SpotifyWhite,
    secondary = SpotifyGreen,
    onSecondary = SpotifyDarkest,
    tertiary = SpotifyWhite,
    onTertiary = SpotifyDarkest,
    background = SpotifyBlack,
    onBackground = SpotifyWhite,
    surface = SpotifySurface,
    onSurface = SpotifyWhite,
    surfaceVariant = SpotifySurfaceElevated,
    onSurfaceVariant = SpotifyMuted,
    surfaceContainer = SpotifySurfaceHighlight,
    outline = SpotifySubdued,
    outlineVariant = SpotifyDivider,
    error = SpotifyError,
    errorContainer = SpotifyErrorContainer,
    onErrorContainer = SpotifyOnErrorContainer
)

@Composable
fun AuraTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Spotify always provides its dedicated, immersive dark audio experience
    MaterialTheme(
        colorScheme = SpotifyDarkColorScheme,
        typography = AuraTypography,
        content = content
    )
}

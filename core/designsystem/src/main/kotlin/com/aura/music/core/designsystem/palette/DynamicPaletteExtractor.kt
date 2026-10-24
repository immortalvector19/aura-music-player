package com.aura.music.core.designsystem.palette

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette

data class ExtractedArtPalette(
    val dominantColor: Color,
    val vibrantColor: Color,
    val mutedColor: Color,
    val onDominantColor: Color
)

object DynamicPaletteExtractor {

    fun extractFromBitmap(bitmap: Bitmap): ExtractedArtPalette {
        val palette = Palette.from(bitmap)
            .maximumColorCount(16) // Fast computation on budget devices (2-3 GB RAM)
            .generate()

        val dominantInt = palette.getDominantColor(0xFF161922.toInt())
        val vibrantInt = palette.getVibrantColor(0xFF7C67FE.toInt())
        val mutedInt = palette.getMutedColor(0xFF1E222D.toInt())

        val dominant = Color(dominantInt)
        val onDominant = if (calculateLuminance(dominant) > 0.5f) Color(0xFF0D0F14) else Color(0xFFF3F4F6)

        return ExtractedArtPalette(
            dominantColor = dominant,
            vibrantColor = Color(vibrantInt),
            mutedColor = Color(mutedInt),
            onDominantColor = onDominant
        )
    }

    private fun calculateLuminance(color: Color): Float {
        return (0.299f * color.red) + (0.587f * color.green) + (0.114f * color.blue)
    }
}

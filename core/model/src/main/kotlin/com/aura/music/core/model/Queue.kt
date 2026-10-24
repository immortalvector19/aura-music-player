package com.aura.music.core.model

enum class RepeatMode {
    OFF,
    ONE,
    ALL
}

enum class ShuffleMode {
    OFF,
    ON
}

data class Queue(
    val tracks: List<Track> = emptyList(),
    val currentIndex: Int = 0,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF
) {
    val currentTrack: Track?
        get() = tracks.getOrNull(currentIndex)

    val hasNext: Boolean
        get() = when (repeatMode) {
            RepeatMode.ONE, RepeatMode.ALL -> tracks.isNotEmpty()
            RepeatMode.OFF -> currentIndex < tracks.size - 1
        }

    val hasPrevious: Boolean
        get() = when (repeatMode) {
            RepeatMode.ONE, RepeatMode.ALL -> tracks.isNotEmpty()
            RepeatMode.OFF -> currentIndex > 0
        }

    fun next(): Queue {
        if (tracks.isEmpty()) return this
        if (repeatMode == RepeatMode.ONE) return this
        val nextIndex = if (currentIndex + 1 < tracks.size) {
            currentIndex + 1
        } else if (repeatMode == RepeatMode.ALL) {
            0
        } else {
            currentIndex
        }
        return copy(currentIndex = nextIndex)
    }

    fun previous(): Queue {
        if (tracks.isEmpty()) return this
        if (repeatMode == RepeatMode.ONE) return this
        val prevIndex = if (currentIndex - 1 >= 0) {
            currentIndex - 1
        } else if (repeatMode == RepeatMode.ALL) {
            tracks.size - 1
        } else {
            0
        }
        return copy(currentIndex = prevIndex)
    }
}

package com.aura.music

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppDestinationTest {

    @Test
    fun destinations_haveExpectedTitlesAndUniqueEntries() {
        val entries = AppDestination.entries
        assertThat(entries).hasSize(3)

        val titles = entries.map { it.title }
        assertThat(titles).containsExactly("Library", "Equalizer", "Settings")
    }

    @Test
    fun appDestinations_haveDistinctIcons() {
        val icons = AppDestination.entries.map { it.icon }.toSet()
        assertThat(icons).hasSize(3)
    }
}

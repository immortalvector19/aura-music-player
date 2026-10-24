package com.aura.music.feature.library

import com.aura.music.core.data.repository.TrackRepository
import com.aura.music.core.data.scanner.MediaStoreScanner
import com.aura.music.core.model.Album
import com.aura.music.core.model.Artist
import com.aura.music.core.model.Track
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: TrackRepository = mockk(relaxed = true)

    private val testTracks = listOf(
        Track(
            id = 1L,
            title = "Solaris",
            artist = "Photay",
            album = "Onism",
            durationMs = 240_000L,
            mediaUri = "content://media/1"
        )
    )

    private lateinit var viewModel: LibraryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getAllTracks() } returns flowOf(testTracks)
        coEvery { repository.getAllAlbums() } returns flowOf(listOf(Album(1L, "Onism", "Photay", 1)))
        coEvery { repository.getAllArtists() } returns flowOf(listOf(Artist(1L, "Photay", 1)))
        coEvery { repository.getStorageLocations() } returns emptyList()

        viewModel = LibraryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsTracksAndDefaultTab() = runTest {
        val tracksJob = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.tracks.collect {}
        }
        val albumsJob = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.albums.collect {}
        }
        val artistsJob = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.artists.collect {}
        }

        testScheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedTab).isEqualTo(LibraryTab.TRACKS)
        assertThat(viewModel.tracks.value).hasSize(1)
        assertThat(viewModel.albums.value).hasSize(1)
        assertThat(viewModel.artists.value).hasSize(1)
    }

    @Test
    fun onTabSelected_updatesSelectedTab() {
        viewModel.onTabSelected(LibraryTab.ALBUMS)
        assertThat(viewModel.uiState.value.selectedTab).isEqualTo(LibraryTab.ALBUMS)

        viewModel.onTabSelected(LibraryTab.ARTISTS)
        assertThat(viewModel.uiState.value.selectedTab).isEqualTo(LibraryTab.ARTISTS)
    }

    @Test
    fun onSearchQueryChanged_updatesSearchQuery() {
        viewModel.onSearchQueryChanged("Solaris")
        assertThat(viewModel.searchQuery.value).isEqualTo("Solaris")
    }

    @Test
    fun onToggleFavorite_delegatesToRepository() = runTest {
        viewModel.onToggleFavorite(1L, true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.toggleFavorite(1L, true) }
    }

    @Test
    fun onScanLibrary_executesAndUpdatesState() = runTest {
        coEvery { repository.scanLibrary() } returns MediaStoreScanner.ScanResult(scannedCount = 10, isSuccess = true)

        viewModel.onScanLibrary()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isScanning).isFalse()
        assertThat(state.scanMessage).contains("Found 10 tracks")
    }
}

package com.aura.music.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.music.core.data.repository.TrackRepository
import com.aura.music.core.model.Album
import com.aura.music.core.model.Artist
import com.aura.music.core.model.StorageInfo
import com.aura.music.core.model.Track
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab {
    TRACKS,
    ALBUMS,
    ARTISTS,
    STORAGE
}

data class LibraryUiState(
    val selectedTab: LibraryTab = LibraryTab.TRACKS,
    val isScanning: Boolean = false,
    val storageLocations: List<StorageInfo> = emptyList(),
    val scanMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val repository: TrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val tracks: StateFlow<List<Track>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllTracks()
            } else {
                repository.searchTracks(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val albums: StateFlow<List<Album>> = repository.getAllAlbums()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val artists: StateFlow<List<Artist>> = repository.getAllArtists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadStorageLocations()
        viewModelScope.launch {
            repository.seedDemoTracksIfEmpty()
        }
    }

    fun onTabSelected(tab: LibraryTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onToggleFavorite(trackId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(trackId, isFavorite)
        }
    }

    fun onScanLibrary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, scanMessage = "Scanning storage...")
            val result = repository.scanLibrary()
            _uiState.value = _uiState.value.copy(
                isScanning = false,
                scanMessage = if (result.isSuccess) "Found ${result.scannedCount} tracks" else "Scan failed"
            )
            loadStorageLocations()
        }
    }

    private fun loadStorageLocations() {
        viewModelScope.launch {
            val locations = repository.getStorageLocations()
            _uiState.value = _uiState.value.copy(storageLocations = locations)
        }
    }
}

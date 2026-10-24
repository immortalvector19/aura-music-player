package com.aura.music.feature.settings

import com.aura.music.core.data.repository.PreferencesRepository
import com.aura.music.core.data.repository.UserPreferences
import com.aura.music.core.data.storage.StorageManager
import com.aura.music.core.model.StorageInfo
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)
    private val storageManager: StorageManager = mockk(relaxed = true)

    private val prefsFlow = MutableStateFlow(
        UserPreferences(
            dataSaverEnabled = false,
            wifiOnlyDownloads = true,
            preferredLanguage = "system",
            audioFocusDucking = true,
            bluetoothAutoResume = true,
            selectedStoragePath = "/storage/emulated/0"
        )
    )

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { preferencesRepository.userPreferencesFlow } returns prefsFlow
        coEvery { storageManager.getAvailableStorageLocations() } returns listOf(
            StorageInfo(
                totalBytes = 64_000_000_000L,
                availableBytes = 12_000_000_000L,
                isExternalSd = false,
                storagePath = "/storage/emulated/0"
            )
        )
        viewModel = SettingsViewModel(preferencesRepository, storageManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsPreferencesAndStorage() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.dataSaver).isFalse()
        assertThat(state.wifiOnlyDownloads).isTrue()
        assertThat(state.audioFocusDucking).isTrue()
        assertThat(state.bluetoothAutoResume).isTrue()
        assertThat(state.preferredLanguage).isEqualTo("system")
        assertThat(state.storageLocations).hasSize(1)
        assertThat(state.hasLowStorage).isFalse()
    }

    @Test
    fun toggleDataSaver_delegatesToRepository() = runTest {
        viewModel.toggleDataSaver(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { preferencesRepository.setDataSaverEnabled(true) }
    }

    @Test
    fun toggleWifiOnly_delegatesToRepository() = runTest {
        viewModel.toggleWifiOnlyDownloads(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { preferencesRepository.setWifiOnlyDownloads(false) }
    }

    @Test
    fun toggleAudioFocusDucking_delegatesToRepository() = runTest {
        viewModel.toggleAudioFocusDucking(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { preferencesRepository.setAudioFocusDucking(false) }
    }

    @Test
    fun toggleBluetoothAutoResume_delegatesToRepository() = runTest {
        viewModel.toggleBluetoothAutoResume(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { preferencesRepository.setBluetoothAutoResume(false) }
    }

    @Test
    fun setPreferredLanguage_delegatesToRepository() = runTest {
        viewModel.setPreferredLanguage("es")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { preferencesRepository.setPreferredLanguage("es") }
    }

    @Test
    fun selectStorageLocation_delegatesToRepository() = runTest {
        viewModel.selectStorageLocation("/storage/SD_CARD")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { preferencesRepository.setSelectedStoragePath("/storage/SD_CARD") }
    }
}

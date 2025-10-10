package com.example.moxmemorygame.ui

import android.os.Build
import androidx.navigation.NavHostController
import com.example.moxmemorygame.data.local.FakeAppSettingsDataStore
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q]) // Specifica l'SDK da simulare
class GameViewModelTest {

    private lateinit var testDispatcher: TestDispatcher

    private lateinit var viewModel: GameViewModel
    private lateinit var fakeDataStore: FakeAppSettingsDataStore
    private lateinit var mockNavController: NavHostController
    private lateinit var fakeTimerViewModel: FakeTimerViewModel

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        fakeDataStore = FakeAppSettingsDataStore()
        mockNavController = mock(NavHostController::class.java)
        fakeTimerViewModel = FakeTimerViewModel()

        viewModel = GameViewModel(
            navController = mockNavController,
            timerViewModel = fakeTimerViewModel,
            appSettingsDataStore = fakeDataStore,
            resourceNameToId = { 0 }
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest(testDispatcher) {
        advanceUntilIdle()
        
        assertThat(viewModel.isBoardInitialized.value).isTrue()
        assertThat(viewModel.moves.intValue).isEqualTo(0)
        assertThat(viewModel.score.intValue).isEqualTo(0)
        assertThat(viewModel.tablePlay).isNotNull()
    }
}
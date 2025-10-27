package com.example.musicboxd.viewModels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.musicboxd.network.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import io.mockk.coEvery
import io.mockk.coVerify


@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    // Esegue LiveData in modo sincrono nei test
    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    // Dispatcher di test per sostituire Dispatchers.Main
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(RetrofitInstance) // permette di intercettare RetrofitInstance.api
    }

    @After
    fun tearDown() {
        unmockkObject(RetrofitInstance)
        Dispatchers.resetMain()
    }

    @Test
    fun `search successo aggiorna tracks con risposta API`() = runTest {
        // Arrange
        val vm = SearchViewModel()

        val t1 = Track(
            id = 1, title = "Fix You",
            artist = Artist("Coldplay"),
            album = Album(1, "X&Y", GenreResponse(emptyList()), cover = "c1", releaseDate = "2005-06-06"),
            duration = 270, preview = null
        )
        val t2 = Track(
            id = 2, title = "Yellow",
            artist = Artist("Coldplay"),
            album = Album(2, "Parachutes", GenreResponse(emptyList()), cover = "c2", releaseDate = "2000-06-26"),
            duration = 260, preview = null
        )
        val fakeResponse = DeezerResponse(data = listOf(t1, t2))

        // Mock API
        val apiMock = mockk<DeezerApi>(relaxed = true)
        coEvery { apiMock.searchTracks("coldplay") } returns DeezerResponse(listOf(t1, t2))
        every { RetrofitInstance.api } returns apiMock

        // Act
        vm.search("coldplay")
        advanceUntilIdle() // svuota viewModelScope

        // Assert
        val value = vm.tracks.value
        assertNotNull(value)
        assertEquals(2, value!!.size)
        assertEquals("Fix You", value[0].title)
        coVerify(exactly = 1) { apiMock.searchTracks("coldplay") }
    }

    @Test
    fun `search errore non crasha e non modifica il valore corrente`() = runTest {
        val vm = SearchViewModel()

        // Stato iniziale nullo
        assertNull(vm.tracks.value)

        // Mock API che lancia eccezione
        val apiMock = mockk<DeezerApi>(relaxed = true)
        coEvery { apiMock.searchTracks(any()) } throws RuntimeException("network down")
        every { RetrofitInstance.api } returns apiMock

        // Act
        vm.search("anything")
        advanceUntilIdle()

        // Assert: il LiveData resta nullo (l'eccezione è catturata)
        assertNull(vm.tracks.value)
        coVerify(exactly = 1) { apiMock.searchTracks("anything") }
    }
}

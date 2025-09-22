import com.teamnotfound.airise.data.serializable.HealthData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import com.teamnotfound.airise.home.HomeViewModel
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelKHealthTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun syncHealthOnEnter_usesMappedValues_fromOverrides() = runTest(dispatcher) {
        val overrides = HomeViewModel.ProviderOverrides(
            requestPermissions = { true },
            getMappedHealthData = {
                HealthData(
                    caloriesBurned = 123,
                    steps = 1200,
                    avgHeartRate = 61,
                    sleep = 6.5f,
                    workout = 3,
                    hydration = 2850f
                )
            }
        )
    }
}
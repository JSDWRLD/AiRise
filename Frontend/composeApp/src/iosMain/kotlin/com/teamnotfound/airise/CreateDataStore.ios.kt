import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.teamnotfound.airise.data.datastore.dataStoreFileName
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask


@OptIn(ExperimentalForeignApi::class)
fun createDataStore(): DataStore<Preferences> {
    val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )

    val path: Path = requireNotNull(documentDirectory).path!!.toPath() / dataStoreFileName

    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { path }
    )
}
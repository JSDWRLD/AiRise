import com.teamnotfound.airise.BuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.BucketApi
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.resumable.ResumableClient
import io.github.jan.supabase.storage.resumable.SettingsResumableCache
import io.github.jan.supabase.storage.storage

object Supabase {

    private val SUPABASE_URL = BuildKonfig.SUPABASE_URL
    private val SUPABASE_KEY = BuildKonfig.SUPABASE_KEY
    private const val BUCKET_NAME = "airise"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Storage)
            BuildKonfig.GEMINI_API_KEY
        }
    }

    val bucket: BucketApi by lazy {
        client.storage[BUCKET_NAME]
    }

}

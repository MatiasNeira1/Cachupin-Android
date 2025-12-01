import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

@Composable
fun FirebaseStorageImage(
    storageRefOrGsUrl: String, // acepta gs://... o "Productos/..."
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    var httpsUrl by remember(storageRefOrGsUrl) { mutableStateOf<String?>(null) }
    var error by remember(storageRefOrGsUrl) { mutableStateOf<String?>(null) }

    LaunchedEffect(storageRefOrGsUrl) {
        httpsUrl = null
        error = null
        try {
            val storage = FirebaseStorage.getInstance()

            val ref = if (storageRefOrGsUrl.startsWith("gs://")) {
                storage.getReferenceFromUrl(storageRefOrGsUrl)
            } else {
                storage.reference.child(storageRefOrGsUrl) // ej: "Productos/comida_perro3.webp"
            }

            httpsUrl = ref.downloadUrl.await().toString()
            Log.d("IMG_STORAGE", "OK -> $httpsUrl")
        } catch (e: CancellationException) {
            // normal cuando haces scroll/recomposición
            Log.d("IMG_STORAGE", "CANCEL")
        } catch (e: Exception) {
            Log.e("IMG_STORAGE", "FAIL from=$storageRefOrGsUrl", e)
            error = e.message ?: "error"
        }
    }

    when {
        httpsUrl != null -> {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(httpsUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
        error != null -> {
            // temporal: muestra el error real para arreglarlo rápido
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text("Imagen no disponible\n$error")
            }
        }
        else -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(26.dp))
            }
        }
    }
}

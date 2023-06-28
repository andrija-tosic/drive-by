import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun CameraCapture(onPhotoCaptured: (Uri) -> Unit) {
    val context = LocalContext.current
    val photoUri = remember { mutableStateOf<Uri?>(null) }

    val takePicture =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri.value?.let { uri ->
                    onPhotoCaptured(uri)
                }
            }
        }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val imageUri = createImageUri(context)
                imageUri?.let {
                    photoUri.value = it
                    takePicture.launch(it)
                }
            } else {
                // Handle permission denied case
            }
        }

    Column {
        if (photoUri.value != null) {
            Text("Photo captured!")
        }
        Button(onClick = {
            val permission = Manifest.permission.CAMERA
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val imageUri = createImageUri(context)
                imageUri?.let {
                    photoUri.value = it
                    takePicture.launch(it)
                }
            } else {
                requestPermissionLauncher.launch(permission)
            }
        }) {
            Text("Launch Camera")
        }
    }
}

private fun createImageUri(context: Context): Uri? {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.TITLE, "New Picture")
        put(MediaStore.Images.Media.DESCRIPTION, "From the camera")
    }
    return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}

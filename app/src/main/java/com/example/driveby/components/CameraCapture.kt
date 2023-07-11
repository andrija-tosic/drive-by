import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.driveby.core.Utils.Companion.showToast

@Composable
fun CameraCapture(onPhotoCaptured: (Uri) -> Unit) {
    val context = LocalContext.current
    val photoUri = remember { mutableStateOf<Uri?>(null) }
    val pictureTaken = remember { mutableStateOf(false)}

    val takePicture =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                pictureTaken.value = true
                photoUri.value?.let { onPhotoCaptured(it) }
            }
        }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val imageUri = createImageUri(context)
                imageUri?.let {
                    takePicture.launch(it)
                }
            } else {
                showToast(context, "Camera permission denied.")
            }
        }

    Column {
        if (pictureTaken.value) {
            Text("Photo captured!")
        }
        OutlinedButton(
            onClick = {
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
            Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Take photo")
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

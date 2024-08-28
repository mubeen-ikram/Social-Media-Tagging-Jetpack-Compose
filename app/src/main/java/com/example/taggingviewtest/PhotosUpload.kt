package com.example.taggingviewtest


import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraHandler(onSuccess: (success: Boolean, image: String?) -> Unit) {
    val context = LocalContext.current

    val showErrorDialogue = remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.CAMERA)

    if (permissionState.status.shouldShowRationale) {
        showErrorDialogue.value = true
    } else if (permissionState.status.isGranted) {
        showCamera = true
    } else {
        LaunchedEffect(key1 = Unit, block = {
            permissionState.launchPermissionRequest()
        })
    }

    CameraLauncherWidget(shouldLaunch = showCamera, isSuccess = {
        showCamera = false
        if (it == true) {
            coroutineScope.launch {
                var path = imageUri.toFileContent(context)
                path = compressFile(context, path)
                onSuccess(true, path)
            }

        } else {
            onSuccess(false, null)
            imageUri = null
        }
    }, resultUri = { imageUri = it })

    PictureErrorDialog(true, showErrorDialogue.value) {
        onSuccess(false, null)
        showErrorDialogue.value = it
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GalleryHandler(maxNoPhotos: Int, onSuccess: (success: Boolean, image: List<String?>) -> Unit) {

    var showErrorDialogue by remember { mutableStateOf(false) }
    var showGallery by remember { mutableStateOf(false) }
    var requestPermission by remember { mutableStateOf(false) }

    val permissionState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            rememberPermissionState(permission = Manifest.permission.READ_MEDIA_IMAGES) {
                if (!it) {
                    onSuccess(false, listOf())
                    requestPermission = false
                }
            }
        else rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) {
            if (!it) {
                onSuccess(false, listOf())
                requestPermission = false
            }
        }

    if (permissionState.status.shouldShowRationale) {
        showErrorDialogue = true
    } else if (permissionState.status.isGranted) {
        showGallery = true
    } else {
        requestPermission = true
        LaunchedEffect(key1 = requestPermission, block = {
            permissionState.launchPermissionRequest()
        })
    }

    GalleryLauncherWidget(showGallery, updateView = {
        showGallery = it
    }, onSuccess = { uris: List<Uri?> ->
        showGallery = false
        onSuccess(uris.isNotEmpty(), uris.map { it?.path })
    }, maxItems = maxNoPhotos)

    PictureErrorDialog(false, showErrorDialogue) {
        onSuccess(false, listOf())
        showErrorDialogue = it
    }
}

@Composable
fun GalleryLauncherWidget(
    shouldLaunch: Boolean,
    onSuccess: (List<Uri?>) -> Unit?,
    updateView: (Boolean) -> Unit,
    maxItems: Int = 1
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val galleryLauncherWidget = if (maxItems > 1) rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems)
    ) { uris: List<Uri?> ->
        val resultPaths = mutableListOf<Uri?>()
        coroutineScope.launch {
            for (uri in uris) {
                uri.let {
                    var path = it.toFileContent(context)
                    path = compressFile(context, path)
                    resultPaths.add(Uri.parse(path))
                }
            }
            onSuccess(resultPaths)
        }
    }
    else rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri == null)
            onSuccess(listOf())
        coroutineScope.launch {
            uri?.let {
                var path = it.toFileContent(context)
                path = compressFile(context, path)
                onSuccess(listOf(Uri.parse(path)))
            }
        }
    }
    if (shouldLaunch) {
        LaunchedEffect(key1 = Unit, block = {
            galleryLauncherWidget.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            updateView(false)
        })
    }
}

@Composable
fun CameraLauncherWidget(
    shouldLaunch: Boolean,
    isSuccess: (Boolean?) -> Unit,
    resultUri: (Uri?) -> Unit,
) {
    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            isSuccess(success)
        }
    )
    if (shouldLaunch) {
        LaunchedEffect(key1 = Unit, block = {
            val uri =
                ComposeFileProvider.getImageUri(context, Random.nextInt())
            resultUri(uri)
            cameraLauncher.launch(uri)
        })
    }
}

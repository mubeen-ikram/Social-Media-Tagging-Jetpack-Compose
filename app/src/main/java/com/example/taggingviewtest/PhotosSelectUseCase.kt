package com.example.taggingviewtest

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource

@Composable
fun PhotosSelectUserCase(modifier: Modifier = Modifier) {
    val handleCaptureImage = remember {
        mutableStateOf(false)
    }
    val handleGalleryImage = remember {
        mutableStateOf(false)
    }

    if (handleGalleryImage.value)
        GalleryHandler(maxNoPhotos = 1, onSuccess = { isSuccess, images ->
            handleGalleryImage.value = false
            if (isSuccess) {
                profileImageState.onImageUriSet(Uri.parse(images.first()))
            }
        })

    if (handleCaptureImage.value)
        CameraHandler(onSuccess = { success, image ->
            handleCaptureImage.value = false
            if (success) {
                profileImageState.onImageUriSet(Uri.parse(image))
            }
        })

    if (showChoiceDialogue.value) {
        FixAlertDialogBoxWithTextAndClickable(
            onDismissRequest = { showChoiceDialogue.value = false },
            bodyCompose = {
                DialogTitle(R.string.please_select_from_the_following)
            },
            cancelButtonText = {
                CancelText(stringResourceId = R.string.photos_gallery)
            },
            cancelButtonClick = {
                handleGalleryImage.value = true
                showChoiceDialogue.value = false
            },
            primaryButtonText = {
                Text(
                    stringResource(R.string.take_photo),
                    color = Color.White
                )
            },
            primaryButtonClick = {
                handleCaptureImage.value = true
                showChoiceDialogue.value = false
            },
        )
    }
}
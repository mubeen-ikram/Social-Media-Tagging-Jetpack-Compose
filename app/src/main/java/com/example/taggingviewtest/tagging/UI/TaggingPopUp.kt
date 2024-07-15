package com.example.taggingviewtest.Tagging.UI

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.taggingviewtest.Tagging.data.TagPopUpUiState
import com.example.taggingviewtest.Tagging.data.TaggedItem

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TaggingPopUp(
    tagPopUpState: TagPopUpUiState,
    modifier: Modifier = Modifier,
    tagPopUpView: @Composable (Modifier, TaggedItem) -> Unit
) {
    val workers = tagPopUpState.taggingList
    if (tagPopUpState.show)
        Box(modifier = modifier.zIndex(1f)) {
            if (workers.isNotEmpty())
                Popup(
                    alignment = Alignment.BottomCenter,
                    onDismissRequest = { },
                ) {
                    Surface(
                        modifier = modifier
                            .semantics {
                                testTagsAsResourceId = true
                                testTag = "comment surface"
                            }
                            .heightIn(0.dp, 300.dp),
                        shape = RoundedCornerShape(5.dp),
                        border = BorderStroke(width = 1.dp, color = Color(0xFF000000))
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth()
                        ) {
                            workers.forEach {
                                tagPopUpView(Modifier, it)
                            }

                        }
                    }
                }
        }
}
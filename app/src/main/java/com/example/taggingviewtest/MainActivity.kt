package com.example.taggingviewtest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.taggingviewtest.Tagging.UI.TaggingPopUp
import com.example.taggingviewtest.Tagging.data.TagPopUpUiState
import com.example.taggingviewtest.Tagging.data.TaggedItem
import com.example.taggingviewtest.ui.theme.TaggingViewTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaggingViewTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CustomViewForPopIpView(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomViewForPopIpView(
    modifier: Modifier = Modifier,
    viewModel: MyViewModel = viewModel()
) {
//    LaunchedEffect(key1 = Unit) {
    val tagList = listOf(
        TaggedItem(1, "First Name", null, data = null),
        TaggedItem(2, "Second Name", null, data = null),
        TaggedItem(3, "Third Name", null, data = null),
        TaggedItem(4, "Fourth Name", null, data = null),
    ).toMutableList()
    viewModel.initTaggingViews(taggingList = tagList, true)
//}
    val tagCommentState by viewModel.commentUiState.collectAsState()
    val tagPopUpState by viewModel.filteredTagsUiState.collectAsState(
        initial = TagPopUpUiState(
            show = false,
            mutableListOf()
        )
    )
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (tagPopUpState.show) {
            for (tag in tagPopUpState.taggingList) {
                Text(
                    text = tag.name ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { viewModel.onItemTagged(tag) })
            }
        }
        BasicTextField(
            value = tagCommentState.textField,
            onValueChange = { viewModel.onInputValueChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, shape = RoundedCornerShape(10.dp), color = Color.Blue)
                .padding(16.dp),
            visualTransformation = {
                TransformedText(
                    viewModel.getResultantString(
                        tagCommentState.textField.annotatedString,
                        tagCommentState.taggedItems
                    ),
                    OffsetMapping.Identity
                )
            },
        )

        Button(onClick = { viewModel.onSuccess() }) {
            Text(text = "Reset The Values")
        }
        Text("The user have tagged following users: ")
        for (tag in tagCommentState.taggedItems) {
            Text(
                text = tag.name ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { })
        }
    }
}

@Composable
fun DefaultViewForPopIpView(
    modifier: Modifier = Modifier,
    viewModel: MyViewModel = viewModel()
) {
    val tagList = listOf(
        TaggedItem(1, "First Name", null, data = null),
        TaggedItem(2, "Second Name", null, data = null),
        TaggedItem(3, "Third Name", null, data = null),
        TaggedItem(4, "Fourth Name", null, data = null),
    ).toMutableList()
    viewModel.initTaggingViews(taggingList = tagList, true)
    val tagCommentState by viewModel.commentUiState.collectAsState()
    val tagPopUpState by viewModel.filteredTagsUiState.collectAsState(
        initial = TagPopUpUiState(
            show = false,
            mutableListOf()
        )
    )
    Column(
        modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TaggingPopUp(
            tagPopUpState = tagPopUpState,
            modifier = Modifier,
        ) { modifier, taggedItem ->
            Column(modifier = modifier.clickable {
                viewModel.onItemTagged(item = taggedItem)
            }) {

                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .height(40.dp)
                            .width(40.dp),
                        shape = CircleShape,
                        border = BorderStroke(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Blue,
                                    Color.White
                                )
                            )
                        )
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .clip(CircleShape),
                            model = taggedItem.profile,
                            contentScale = ContentScale.Crop,
                            contentDescription = null
                        )

                    }
                    Text(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        text = taggedItem.name ?: ""
                    )

                }
                HorizontalDivider(
                    modifier = Modifier,
                )
            }
        }
        BasicTextField(
            value = tagCommentState.textField,
            onValueChange = { viewModel.onInputValueChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, shape = RoundedCornerShape(10.dp), color = Color.Blue)
                .padding(16.dp),
            visualTransformation = {
                TransformedText(
                    viewModel.getResultantString(
                        tagCommentState.textField.annotatedString,
                        tagCommentState.taggedItems
                    ),
                    OffsetMapping.Identity
                )
            },
        )

        Button(onClick = { viewModel.onSuccess() }) {
            Text(text = "Reset The Values")
        }
        Text("The user have tagged following users: ")
        for (tag in tagCommentState.taggedItems) {
            Text(
                text = tag.name ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomTaggingPreview() {
    TaggingViewTestTheme {
        CustomViewForPopIpView()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultTaggingPreview() {
    TaggingViewTestTheme {
        DefaultViewForPopIpView()
    }
}
package com.example.taggingviewtest.Tagging.data

import androidx.compose.ui.text.input.TextFieldValue


data class TaggingViewState(
    val textField: TextFieldValue,
    val taggedItems: MutableList<TaggedItem,>,
    val loading: Boolean,
    val errorString: String,
    val enable: Boolean,
)
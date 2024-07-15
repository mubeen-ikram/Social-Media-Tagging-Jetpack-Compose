package com.example.taggingviewtest.Tagging.data

sealed interface TextEditState {
    object Add : TextEditState
    object Default : TextEditState
    data class Edit(
        val indexChanged: Int,
        val changedLength: Int,
    ) : TextEditState
    data class Removed(
        val indexChanged: Int,
        val changedLength: Int,
    ) : TextEditState
}
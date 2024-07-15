package com.example.taggingviewtest.Tagging.data

data class TagPopUpUiState(
    val show: Boolean = false,
    val taggingList: List<TaggedItem>
){
    companion object{
        val defaultValue = TagPopUpUiState(show = true, listOf())
    }
}
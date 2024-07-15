package com.example.taggingviewtest.Tagging.data

data class TaggedItem(
    val id: Int,
    val name: String?,
    val profile: String?,
    var indexStart: Int = -1,
    var indexEnd: Int = -1,
    val data: Any?
)

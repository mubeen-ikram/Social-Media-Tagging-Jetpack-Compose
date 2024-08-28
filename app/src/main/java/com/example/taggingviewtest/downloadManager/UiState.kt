package com.example.taggingviewtest.downloadManager


sealed class UiState<T>(val data: T) {
    class Loading<T>(data: T) : UiState<T>(data)
    class Default<T>(data: T) : UiState<T>(data)
    class Error<T>(data: T, val error: Throwable) : UiState<T>(data)
}
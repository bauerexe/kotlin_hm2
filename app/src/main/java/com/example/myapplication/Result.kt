package com.example.myapplication

sealed class Result<out T> {
    data class Ok<out T>(val data: T) : Result<T>()
    data class Error(val error: String) : Result<Nothing>()
}

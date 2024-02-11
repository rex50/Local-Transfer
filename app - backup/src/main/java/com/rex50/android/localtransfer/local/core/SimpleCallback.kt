package com.rex50.android.localtransfer.local.core

interface SimpleCallback<T> {

    fun onError(errorMsg: String) {}

    fun onSuccess(data: T) {}
}
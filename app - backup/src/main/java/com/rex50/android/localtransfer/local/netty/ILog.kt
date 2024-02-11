package com.rex50.android.localtransfer.local.netty

interface ILog {

    fun d(tag: String, msg: String)

    fun e(tag: String, msg: String, throwable: Throwable? = null)
}
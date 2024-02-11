package com.rex50.android.localtransfer.local.netty.extensions

import com.tans.tfiletransporter.netty.PackageData

interface IBodyConverter {

    fun couldHandle(type: Int, dataClass: Class<*>): Boolean

    fun <T> convert(type: Int, dataClass: Class<T>, packageData: PackageData): T?
}
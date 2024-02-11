package com.rex50.android.localtransfer.local.netty.extensions

import com.tans.tfiletransporter.netty.PackageData

interface IPackageDataConverter {

    fun couldHandle(type: Int, dataClass: Class<*>): Boolean

    fun <T> convert(type: Int, messageId: Long, data: T, dataClass: Class<T>): PackageData?
}
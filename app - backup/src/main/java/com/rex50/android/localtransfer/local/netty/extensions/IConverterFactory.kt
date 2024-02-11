package com.rex50.android.localtransfer.local.netty.extensions

interface IConverterFactory {

    fun findBodyConverter(type: Int, dataClass: Class<*>) : IBodyConverter?

    fun findPackageDataConverter(type: Int, dataClass: Class<*>): IPackageDataConverter?
}
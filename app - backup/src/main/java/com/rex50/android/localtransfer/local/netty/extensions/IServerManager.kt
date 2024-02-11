package com.rex50.android.localtransfer.local.netty.extensions

interface IServerManager {

    fun <Request, Response> registerServer(s: IServer<Request, Response>)

    fun <Request, Response> unregisterServer(s: IServer<Request, Response>)

    fun clearAllServers()
}
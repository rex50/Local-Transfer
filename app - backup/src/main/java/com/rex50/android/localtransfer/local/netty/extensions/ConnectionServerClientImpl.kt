package com.rex50.android.localtransfer.local.netty.extensions

import com.rex50.android.localtransfer.local.netty.INettyConnectionTask

class ConnectionServerClientImpl(
    val connectionTask: INettyConnectionTask,
    val serverManager: IServerManager,
    val clientManager: IClientManager
) : INettyConnectionTask by connectionTask, IServerManager by serverManager, IClientManager by clientManager
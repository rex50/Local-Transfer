package com.rex50.android.localtransfer.local.netty.extensions

import com.rex50.android.localtransfer.local.netty.ILog
import com.rex50.android.localtransfer.local.netty.INettyConnectionTask

class ConnectionClientImpl(
    val connectionTask: INettyConnectionTask,
    val clientManager: IClientManager
) : INettyConnectionTask by connectionTask, IClientManager by clientManager


@Suppress("UNCHECKED_CAST")
fun <T> INettyConnectionTask.withClient(
    converterFactory: IConverterFactory = DefaultConverterFactory(),
    log: ILog
): T {
    return when (this) {
        is ConnectionClientImpl -> {
            val impl = DefaultClientManager(
                connectionTask = this.connectionTask,
                converterFactory = converterFactory,
                log = log
            )
            ConnectionClientImpl(
                connectionTask = this.connectionTask,
                clientManager = impl
            ) as T
        }
        is ConnectionServerImpl -> {
            val impl = DefaultClientManager(
                connectionTask = this.connectionTask,
                converterFactory = converterFactory,
                log = log
            )
            ConnectionServerClientImpl(
                connectionTask = this.connectionTask,
                clientManager = impl,
                serverManager = this.serverManager
            ) as T
        }

        is ConnectionServerClientImpl -> {
            val impl = DefaultClientManager(
                connectionTask = this.connectionTask,
                converterFactory = converterFactory,
                log = log
            )
            ConnectionServerClientImpl(
                connectionTask = this.connectionTask,
                clientManager = impl,
                serverManager = this.serverManager
            ) as T
        }
        else -> {
            val impl = DefaultClientManager(
                connectionTask = this,
                converterFactory = converterFactory,
                log = log
            )
            ConnectionClientImpl(
                connectionTask = this,
                clientManager = impl
            ) as T
        }
    }
}
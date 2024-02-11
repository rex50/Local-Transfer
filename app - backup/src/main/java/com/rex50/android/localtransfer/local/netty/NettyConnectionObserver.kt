package com.rex50.android.localtransfer.local.netty

import com.tans.tfiletransporter.netty.NettyTaskState
import com.tans.tfiletransporter.netty.PackageData
import java.net.InetSocketAddress

interface NettyConnectionObserver {

    fun onNewState(nettyState: NettyTaskState, task: INettyConnectionTask)

    fun onNewMessage(
        localAddress: InetSocketAddress?,
        remoteAddress: InetSocketAddress?,
        msg: PackageData,
        task: INettyConnectionTask
    )
}
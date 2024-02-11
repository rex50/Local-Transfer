package com.rex50.android.localtransfer.local.p2p.conn

import java.net.InetSocketAddress

sealed class P2pConnectionState {

    object NoConnection : P2pConnectionState()

    object Requesting : P2pConnectionState()

    class Active(
        val localAddress: InetSocketAddress?,
        val remoteAddress: InetSocketAddress?
    ) : P2pConnectionState()

    data class Handshake(
        val localAddress: InetSocketAddress,
        val remoteAddress: InetSocketAddress,
        val remoteDeviceName: String
    ) : P2pConnectionState()
}
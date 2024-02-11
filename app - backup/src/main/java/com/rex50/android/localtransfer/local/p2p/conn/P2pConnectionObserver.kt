package com.rex50.android.localtransfer.local.p2p.conn

interface P2pConnectionObserver {

    fun onNewState(state: P2pConnectionState)

    fun requestTransferFile(handshake: P2pConnectionState.Handshake, isReceiver: Boolean)
}
package com.rex50.android.localtransfer.local.p2p.conn

import com.rex50.android.localtransfer.local.core.SimpleCallback
import com.rex50.android.localtransfer.local.core.resumeExceptionIfActive
import com.rex50.android.localtransfer.local.core.resumeIfActive
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.InetAddress

suspend fun P2pConnection.bindSuspend(address: InetAddress) = suspendCancellableCoroutine { cont ->
    bind(address, object : SimpleCallback<Unit> {
        override fun onSuccess(data: Unit) {
            cont.resumeIfActive(Unit)
        }

        override fun onError(errorMsg: String) {
            cont.resumeExceptionIfActive(Throwable(errorMsg))
        }
    })
}

suspend fun P2pConnection.connectSuspend(address: InetAddress) =
    suspendCancellableCoroutine { cont ->
        connect(address, object : SimpleCallback<Unit> {
            override fun onSuccess(data: Unit) {
                cont.resumeIfActive(Unit)
            }

            override fun onError(errorMsg: String) {
                cont.resumeExceptionIfActive(Throwable(errorMsg))
            }
        })
    }

suspend fun P2pConnection.closeSuspend() = suspendCancellableCoroutine { cont ->
    requestClose(object : SimpleCallback<Unit> {

        override fun onSuccess(data: Unit) {
            cont.resumeIfActive(Unit)
        }

        override fun onError(errorMsg: String) {
            cont.resumeExceptionIfActive(Throwable(errorMsg))
        }
    })
}

suspend fun P2pConnection.transferFileSuspend() = suspendCancellableCoroutine<Unit> { cont ->
    requestTransferFile(object : SimpleCallback<P2pConnectionState.Handshake> {

        override fun onSuccess(data: P2pConnectionState.Handshake) {
            cont.resumeIfActive(Unit)
        }

        override fun onError(errorMsg: String) {
            cont.resumeExceptionIfActive(Throwable(errorMsg))
        }
    })
}

suspend fun P2pConnection.waitHandshaking() =
    suspendCancellableCoroutine<P2pConnectionState.Handshake> { cont ->
        addObserver(object : P2pConnectionObserver {
            init {
                cont.invokeOnCancellation { removeObserver(this) }
            }

            override fun onNewState(state: P2pConnectionState) {
                if (state is P2pConnectionState.Handshake) {
                    cont.resumeIfActive(state)
                    removeObserver(this)
                }
                if (state is P2pConnectionState.NoConnection) {
                    cont.resumeExceptionIfActive(Throwable("Connection closed"))
                    removeObserver(this)
                }
            }

            override fun requestTransferFile(
                handshake: P2pConnectionState.Handshake,
                isReceiver: Boolean
            ) {
            }
        })
    }

suspend fun P2pConnection.waitClose() = suspendCancellableCoroutine<Unit> { cont ->
    addObserver(object : P2pConnectionObserver {
        init {
            cont.invokeOnCancellation { removeObserver(this) }
        }

        override fun onNewState(state: P2pConnectionState) {
            if (state is P2pConnectionState.NoConnection) {
                cont.resumeIfActive(Unit)
                removeObserver(this)
            }
        }

        override fun requestTransferFile(
            handshake: P2pConnectionState.Handshake,
            isReceiver: Boolean
        ) {
        }
    })
}
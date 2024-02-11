package com.rex50.android.localtransfer.local.p2p

import android.annotation.SuppressLint
import android.net.wifi.p2p.*
import android.os.Build
import androidx.annotation.RequiresApi
import com.rex50.android.localtransfer.local.Device
import com.rex50.android.localtransfer.local.core.resumeIfActive
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

enum class WifiActionResult(val code: Int) {
    Success(-1),
    Error(WifiP2pManager.ERROR),
    Busy(WifiP2pManager.BUSY),
    Unsupported(WifiP2pManager.P2P_UNSUPPORTED)
}

@SuppressLint("MissingPermission")
suspend fun WifiP2pManager.discoverPeersSuspend(channel: WifiP2pManager.Channel) = suspendCancellableCoroutine<WifiActionResult> { cont ->
    discoverPeers(channel, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            cont.resumeIfActive(WifiActionResult.Success)
        }

        override fun onFailure(reason: Int) {
            cont.resumeIfActive(WifiActionResult.values().first { it.code == reason })
        }

    })
}

@SuppressLint("MissingPermission")
suspend fun WifiP2pManager.requestPeersSuspend(channel: WifiP2pManager.Channel) = suspendCancellableCoroutine<WifiP2pDeviceList?> { cont ->
    requestPeers(channel) { cont.resume(it) }
}

@SuppressLint("MissingPermission")
suspend fun WifiP2pManager.connectSuspend(channel: WifiP2pManager.Channel, config: WifiP2pConfig) = suspendCancellableCoroutine { cont ->
    connect(channel, config, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            cont.resumeIfActive(WifiActionResult.Success)
        }

        override fun onFailure(reason: Int) {
            cont.resumeIfActive(WifiActionResult.values().first { it.code == reason })
        }

    })
}

suspend fun WifiP2pManager.requestConnectionInfoSuspend(channel: WifiP2pManager.Channel) = suspendCancellableCoroutine { cont ->
    requestConnectionInfo(channel) { info ->
        cont.resumeIfActive(if (info?.groupOwnerAddress == null) null else info)
    }
}

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.Q)
suspend fun WifiP2pManager.requestDeviceInfoSuspend(channel: WifiP2pManager.Channel) = suspendCancellableCoroutine { cont ->
    requestDeviceInfo(channel) { device -> if (cont.isActive) cont.resume(device) }
}

suspend fun WifiP2pManager.cancelConnectionSuspend(channel: WifiP2pManager.Channel) = suspendCancellableCoroutine { cont ->
    cancelConnect(channel, object : WifiP2pManager.ActionListener {

        override fun onSuccess() {
            cont.resumeIfActive(WifiActionResult.Success)
        }

        override fun onFailure(reason: Int) {
            cont.resumeIfActive(WifiActionResult.values().first { it.code == reason })
        }

    })
}

suspend fun WifiP2pManager.removeGroupSuspend(channel: WifiP2pManager.Channel) = suspendCancellableCoroutine<WifiActionResult> { cont ->
    removeGroup(channel, object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            cont.resumeIfActive(WifiActionResult.Success)
        }

        override fun onFailure(reason: Int) {
            cont.resumeIfActive(WifiActionResult.values().first { it.code == reason })
        }

    })
}

fun WifiP2pDevice.toDevice(): Device {
    return Device(
        name = deviceName,
        address = deviceAddress
    )
}
package com.rex50.android.localtransfer.local

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.net.InetAddress

interface LocalConnector {

    val availableDevices: StateFlow<List<Device>>

    val connectionState: StateFlow<LocalConnectionState>

    suspend fun init(activity: Activity)

    suspend fun turnOnHostMode()

    suspend fun scanForAvailableDevices()

    suspend fun requestConnection(device: Device)

    suspend fun disconnect()

    fun transferFileRequest(file: File)

    fun onResume()

    fun onPause()

    fun onDestroy()
}

data class Device(
    val name: String,
    val address: String
)

sealed class RequestState<T> {
    data class Success<T>(val data: T): RequestState<T>()
    data class Failure<T>(val exception: Exception) : RequestState<T>()
}

sealed class LocalConnectionState {
    object NoConnection : LocalConnectionState()
    object Connecting : LocalConnectionState()
    object Ready : LocalConnectionState()
    data class Connected(val device: Device) : LocalConnectionState()

    data class ConnectionInfo(
        val isGroupOwner: Boolean,
        val groupOwnerAddress: InetAddress
    ): LocalConnectionState()

    data class Transferring(
        val progress: Int,
        val speed: Int,
        val totalSize: Long,
        val transferredSize: Long
    ) : LocalConnectionState()

    object Disconnected : LocalConnectionState()

    data class Error(val e: Exception) : LocalConnectionState()
}
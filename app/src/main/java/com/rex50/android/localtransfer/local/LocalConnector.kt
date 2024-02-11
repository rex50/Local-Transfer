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

    suspend fun disconnectAsClient()

    suspend fun disconnectAsHost()

    fun checkConnectionState()

    fun transferFileRequest(file: File)

    fun onResume()

    fun onPause()

    fun onDestroy()
}

data class Device(
    val name: String,
    val address: String
)

sealed class LocalConnectionState {

    // Failed while connecting
    object NoConnection : LocalConnectionState()

    // Connecting to a device
    object Connecting : LocalConnectionState()

    // Ready for a connection
    object Ready : LocalConnectionState()

    // Connected to a device
    data class Connected(val device: Device) : LocalConnectionState()

    // Connected group info
    data class ConnectedGroupInfo(
        val isGroupOwner: Boolean,
        val groupOwnerAddress: InetAddress
    ): LocalConnectionState()

    // Data transferring
    data class Transferring(
        val progress: Int,
        val speed: Int,
        val totalSize: Long,
        val transferredSize: Long
    ) : LocalConnectionState()

    // Error while performing a action
    data class Error(val e: Exception) : LocalConnectionState()
}
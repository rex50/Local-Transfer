package com.rex50.android.localtransfer.local.p2p

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.util.Log
import com.rex50.android.localtransfer.local.Device
import com.rex50.android.localtransfer.local.LocalConnectionState
import com.rex50.android.localtransfer.local.LocalConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class WifiP2pLocalConnector : LocalConnector {

    private val connectionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _availableDevices = MutableStateFlow<List<Device>>(listOf())
    override val availableDevices: StateFlow<List<Device>> = _availableDevices.asStateFlow()

    private val _connectionState =
        MutableStateFlow<LocalConnectionState>(LocalConnectionState.NoConnection)
    override val connectionState: StateFlow<LocalConnectionState> = _connectionState.asStateFlow()

    private val wifiP2pManager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        activity?.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
    }

    private var activity: Activity? = null

    private var wifiChannel: WifiP2pManager.Channel? = null

    private var wifiP2PConnection: LocalConnectionState.ConnectedGroupInfo? = null

    val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }


    private val wifiReceiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {

                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    // Check to see if Wi-Fi is enabled and notify appropriate activity
                    connectionScope.launch {
                        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                        if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                            Log.e(TAG, "Wifi p2p disabled.")
                            _connectionState.emit(LocalConnectionState.Error(IllegalStateException("P2P Disabled")))
                        } else {
                            Log.d(TAG, "Wifi p2p enabled.")
                            _connectionState.emit(LocalConnectionState.Ready)
                        }
                    }
                }

                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    // Call WifiP2pManager.requestPeers() to get a list of current peers
                    connectionScope.launch {
                        val wifiDevicesList =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                intent.getParcelableExtra(
                                    WifiP2pManager.EXTRA_P2P_DEVICE_LIST,
                                    WifiP2pDeviceList::class.java
                                )
                            } else {
                                intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST)
                            }
                        val devices = wifiDevicesList?.deviceList?.map { it.toDevice() } ?: listOf()
                        Log.d(
                            TAG,
                            "WIFI p2p devices: ${devices.joinToString { "${it.name} -> ${it.address}" }}"
                        )
                        _availableDevices.emit(devices)
                    }
                }

                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    // Respond to new connection or disconnections
                    Log.d(TAG, "Connection state change.")
                    connectionScope.launch {
                        checkWifiConnection()
                    }
                }

                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    // Respond to this device's wifi state changing
                    // Android 10 can't get mac address.
                }
            }
        }
    }

    override suspend fun init(activity: Activity) {
        this.activity = activity
        wifiChannel = wifiP2pManager?.initialize(activity, activity.mainLooper, null)
        // TODO: UnspecifiedRegisterReceiverFlag
        activity.registerReceiver(wifiReceiver, intentFilter)
    }

    override suspend fun turnOnHostMode() {
        // Host is not required here, so do nothing
    }

    override suspend fun scanForAvailableDevices() {
        wifiChannel?.let { channel ->
            if (wifiP2PConnection == null) {
                val state = wifiP2pManager?.discoverPeersSuspend(channel)
                if (state == WifiActionResult.Success) {
                    Log.d(TAG, "Request discover peer success")
                    val wifiDevicesList = wifiP2pManager?.requestPeersSuspend(channel = channel)
                    val devices = wifiDevicesList?.deviceList?.map { it.toDevice() } ?: listOf()
                    Log.d(
                        TAG,
                        "WIFI p2p devices: ${devices.joinToString { "${it.name} -> ${it.address}" }}"
                    )
                    _availableDevices.emit(devices)
                } else {
                    _availableDevices.emit(emptyList())
                    Log.e(TAG, "Request discover peer fail: $state")
                }
            } else {
                _availableDevices.emit(emptyList())
            }
        }
    }

    override suspend fun requestConnection(device: Device) {
        val config = WifiP2pConfig()
        config.deviceAddress = device.address
        val channel = wifiChannel ?: return

        _connectionState.emit(LocalConnectionState.Connecting)
        val result = wifiP2pManager?.connectSuspend(channel, config)
        if (result == WifiActionResult.Success) {
            _connectionState.emit(LocalConnectionState.Connected(device))
            Log.d(TAG, "Request P2P connection success !!!")
            val connectionInfo = wifiP2pManager?.requestConnectionInfoSuspend(channel)
            if(connectionInfo != null) {
                val info = LocalConnectionState.ConnectedGroupInfo(
                    isGroupOwner = connectionInfo.isGroupOwner,
                    groupOwnerAddress = connectionInfo.groupOwnerAddress
                )
                wifiP2PConnection = info
                _connectionState.emit(info)
            }
            Log.d(TAG, "requestConnectionL: Connection group address: ${connectionInfo?.groupOwnerAddress}, is group owner: ${connectionInfo?.isGroupOwner}")
        } else {
            _connectionState.emit(LocalConnectionState.NoConnection)
            Log.e(TAG, "Request P2P connection fail: $result !!!")
        }
    }

    override suspend fun disconnectAsClient() {
        wifiChannel?.let {
            val hostResult = wifiP2pManager?.removeGroupSuspend(it)
            if(hostResult == WifiActionResult.Success) {
                _connectionState.emit(LocalConnectionState.NoConnection)
            } else {
                _connectionState.emit(LocalConnectionState.Error(IllegalStateException("Problem while disconnecting as client")))
            }
        }
    }

    override suspend fun disconnectAsHost() {
        wifiChannel?.let {
            val hostResult = wifiP2pManager?.removeGroupSuspend(it)
            if(hostResult == WifiActionResult.Success) {
                _connectionState.emit(LocalConnectionState.NoConnection)
            } else {
                _connectionState.emit(LocalConnectionState.Error(IllegalStateException("Problem while disconnecting as host")))
            }
        }
    }

    override fun checkConnectionState() {
        connectionScope.launch {
            checkWifiConnection()
        }
    }

    override fun transferFileRequest(file: File) {
        // TODO: Transfer file
    }

    override fun onResume() {
        // Unused for now
    }

    override fun onPause() {
        // Unused for now
    }

    override fun onDestroy() {
        connectionScope.launch {
            activity?.unregisterReceiver(wifiReceiver)
            disconnectAsClient()
            disconnectAsHost()
        }
    }

    private suspend fun checkWifiConnection(): LocalConnectionState.ConnectedGroupInfo? {
        val connectionOld = wifiP2PConnection
        val channel = wifiChannel ?: return null
        val connectionNew =
            wifiP2pManager?.requestConnectionInfoSuspend(channel)?.let {
                LocalConnectionState.ConnectedGroupInfo(
                    isGroupOwner = it.isGroupOwner,
                    groupOwnerAddress = it.groupOwnerAddress
                )
            }
        Log.d(
            TAG,
            "checkWifiConnection: Connection group address: ${connectionNew?.groupOwnerAddress}, is group owner: ${connectionNew?.isGroupOwner}"
        )
        if (connectionNew != connectionOld) {
            if(connectionNew == null)
                _connectionState.emit(LocalConnectionState.NoConnection)
            else {
                val connectionInfo = LocalConnectionState.ConnectedGroupInfo(
                    isGroupOwner = connectionNew.isGroupOwner,
                    groupOwnerAddress = connectionNew.groupOwnerAddress
                )
                wifiP2PConnection = connectionInfo
                _connectionState.emit(connectionInfo)
            }
        }
        return connectionNew
    }

    companion object {
        private const val TAG = "WifiP2pLocalConnector"
    }
}
package com.rex50.android.localtransfer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.rex50.android.localtransfer.local.Device
import com.rex50.android.localtransfer.local.LocalConnectionState
import com.rex50.android.localtransfer.local.LocalConnector
import com.rex50.android.localtransfer.local.p2p.WifiP2pLocalConnector
import com.rex50.android.localtransfer.ui.theme.LocalTransferTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val localConnector: LocalConnector = WifiP2pLocalConnector()

    private var status by mutableStateOf("Scanning...")
    private var connectedDevice by mutableStateOf<Device?>(null)
    private var connectedGroupInfo by mutableStateOf<LocalConnectionState.ConnectedGroupInfo?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocalTransferTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val coroutineScope = rememberCoroutineScope() 
                    val devices by localConnector.availableDevices.collectAsState()
                    Column {
                        Status(
                            status = status,
                            isDisconnectVisible = true,
                            onRefresh = {
                                coroutineScope.launch {
                                    localConnector.scanForAvailableDevices()
                                    localConnector.checkConnectionState()
                                }
                            },
                            onDelete = {
                                coroutineScope.launch {
                                    if(connectedGroupInfo == null) {
                                        localConnector.disconnectAsHost()
                                        localConnector.disconnectAsClient()
                                        return@launch
                                    }
                                    if(connectedGroupInfo?.isGroupOwner == true)
                                        localConnector.disconnectAsHost()
                                    else
                                        localConnector.disconnectAsClient()
                                }
                            }
                        )

                        AnimatedVisibility(visible = connectedDevice != null) {
                            Text(
                                text = "Name: ${connectedDevice?.name}\naddress: ${connectedDevice?.address}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            )
                        }

                        AnimatedVisibility(visible = connectedGroupInfo != null) {
                            Text(
                                text = "Group Owner: ${connectedGroupInfo?.isGroupOwner}\nGroup owner address: ${connectedGroupInfo?.groupOwnerAddress}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            )
                        }

                        AnimatedVisibility(connectedDevice == null || connectedGroupInfo == null) {
                            AvailableDevicesContent(devices) {
                                coroutineScope.launch {
                                    localConnector.requestConnection(it)
                                }
                            }
                        }
                    }
                }
            }
        }
        initLocalConnector()
    }

    private fun initLocalConnector() = lifecycleScope.launch {
        localConnector.init(this@MainActivity)
        localConnector.scanForAvailableDevices()
        localConnector.connectionState.collectLatest {
            Log.d("MainActivity", "onCreate: $it")
            when(it) {
                is LocalConnectionState.Connected -> {
                    connectedDevice = it.device
                    status = "Connected to ${it.device.name}"
                }

                LocalConnectionState.Connecting -> status = "Connecting..."

                is LocalConnectionState.ConnectedGroupInfo -> {
                    connectedGroupInfo = it
                }

                is LocalConnectionState.Error -> {
                    connectedDevice = null
                    connectedGroupInfo = null
                    status = "Error ${it.e.message}"
                }

                LocalConnectionState.NoConnection -> {
                    status = "No connection"
                    connectedDevice = null
                    connectedGroupInfo = null
                }

                LocalConnectionState.Ready -> status = "Ready"

                is LocalConnectionState.Transferring -> status = "Transferring data"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        localConnector.onDestroy()
    }
}

/**
 * Status Component
 */
@Composable
fun Status(
    status: String,
    isDisconnectVisible: Boolean,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Image(
            painter = rememberVectorPainter(image = Icons.Default.Refresh),
            contentDescription = "Rescan",
            modifier = Modifier
                .padding(16.dp)
                .clickable {
                    onRefresh()
                }
        )
        Text(
            text = status,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        )

        AnimatedVisibility(isDisconnectVisible) {
            Text(
                text = "Disconnect",
                fontSize = 14.sp,
                color = Color.Red,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable {
                        onDelete()
                    }
                    .padding(16.dp)
            )
        }

    }

}

@ThemedComponentPreviews
@Composable
private fun StatusPreview() {
    Preview {
        Status(
            status = "Idle",
            isDisconnectVisible = true,
            onDelete =  {},
            onRefresh =  {}
        )
    }
}


@Composable
fun AvailableDevicesContent(
    devices: List<Device>,
    modifier: Modifier = Modifier,
    onClicked: (Device) -> Unit
) {
    LazyColumn(modifier) {
        items(devices) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        onClicked(it)
                    }
                    .padding(16.dp)
            ) {
                Text(
                    text = it.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = it.address,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@ThemedComponentPreviews
@Composable
fun AvailableDevicesContentPreview() {
    LocalTransferTheme {
        AvailableDevicesContent(
            listOf(
                Device("POCO F1", "9803485"),
                Device("Redmi K20 Pro", "5478906"),
                Device("Realme X2 Pro", "897345"),
            )
        ) {

        }
    }
}

@Composable
fun Preview(content: @Composable () -> Unit) {
    LocalTransferTheme {
        content()
    }
}

@Preview(showBackground = true)
annotation class ThemedComponentPreviews
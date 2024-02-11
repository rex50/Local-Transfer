package com.rex50.android.localtransfer

import android.net.http.UrlRequest.Status
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
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
                    var status = remember {
                        "Idle"
                    }
                    var connectedDevice: Device? = remember {
                        null
                    }
                    var connectionInfo: LocalConnectionState.ConnectionInfo? = remember {
                        null
                    }

                    LaunchedEffect(Unit) {
                        localConnector.connectionState.collectLatest {
                            when(it) {
                                is LocalConnectionState.Connected -> {
                                    connectedDevice = it.device
                                    status = "Connected to ${it.device.name}"
                                }
                                LocalConnectionState.Connecting -> status = "Connecting..."
                                is LocalConnectionState.ConnectionInfo -> {
                                    connectionInfo = connectionInfo
                                }
                                LocalConnectionState.Disconnected -> {
                                    connectedDevice = null
                                    connectionInfo = null
                                    status = "Disconnected"

                                }
                                is LocalConnectionState.Error -> {
                                    status = "Error ${it.e.message}"
                                }
                                LocalConnectionState.NoConnection -> status = "No connection"
                                LocalConnectionState.Ready -> status = "Ready"
                                is LocalConnectionState.Transferring -> status = "Transferring data"
                            }
                        }
                    }



                    Column {
                        Status(status = status)

                        AnimatedVisibility(connectedDevice == null) {
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
        lifecycleScope.launch {
            localConnector.init(this@MainActivity)
        }
    }
}

/**
 * Status Component
 */
@Composable
fun Status(status: String) {
    Text(
        text = status,
        fontSize = 14.sp,
        color = Color.DarkGray,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@ThemedComponentPreviews
@Composable
private fun StatusPreview() {
    Preview {
        Status("Idle")
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
                    .background(Color.LightGray, RoundedCornerShape(16.dp))
                    .clickable {
                        onClicked(it)
                    }
                    .padding(16.dp)
            ) {
                Text(
                    text = it.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = it.address,
                    fontSize = 12.sp
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
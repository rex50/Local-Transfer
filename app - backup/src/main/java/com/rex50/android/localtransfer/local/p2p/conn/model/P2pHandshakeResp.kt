package com.rex50.android.localtransfer.local.p2p.conn.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class P2pHandshakeResp(
    val deviceName: String
)
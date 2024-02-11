package com.rex50.android.localtransfer.local.p2p.conn.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Keep
data class P2pHandshakeReq(
    val version: Int,
    val deviceName: String
)
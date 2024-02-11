package com.rex50.android.localtransfer.local.p2p.conn.model

enum class P2pDataType(val type: Int) {
    HandshakeReq(0),
    HandshakeResp(1),
    TransferFileReq(2),
    TransferFileResp(3),
    CloseConnReq(4),
    CloseConnResp(5)
}
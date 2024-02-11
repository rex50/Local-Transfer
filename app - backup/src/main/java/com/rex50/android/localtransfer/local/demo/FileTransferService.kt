// Copyright 2011 Google Inc. All Rights Reserved.

package com.rex50.android.localtransfer.local.demo

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import android.util.Log

import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
class FileTransferService(name: String) : IntentService(name) {

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(p0: Intent?) {

        val intent = p0 ?: return
        val context = applicationContext
        if (intent.action == ACTION_SEND_FILE) {
            val fileUri = intent.extras!!.getString(EXTRAS_FILE_PATH)
            val host = intent.extras!!.getString(EXTRAS_GROUP_OWNER_ADDRESS)
            val socket = Socket()
            val port = intent.extras!!.getInt(EXTRAS_GROUP_OWNER_PORT)

            try {
                Log.d(TAG, "Opening client socket - ")
                socket.bind(null)
                socket.connect(InetSocketAddress(host, port), SOCKET_TIMEOUT)

                Log.d(TAG, "Client socket - " + socket.isConnected)
                val stream = socket.getOutputStream()
                val cr = context.contentResolver
                try {
                    cr.openInputStream(Uri.parse(fileUri))?.use {
                        DeviceDetailFragment.copyFile(it, stream)
                    }
                } catch (e: FileNotFoundException) {
                    Log.d(TAG, e.toString())
                }
                Log.d(TAG, "Client: Data written")
            } catch (e: IOException) {
                Log.e(TAG, e.message ?: "")
            } finally {
                if (socket.isConnected) {
                    try {
                        socket.close()
                    } catch (e: IOException) {
                        // Give up
                        e.printStackTrace()
                    }
                }
            }

        }
    }

    companion object {

        private val TAG = "FileTransferService"
        private const val SOCKET_TIMEOUT = 5000
        const val ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE"
        const val EXTRAS_FILE_PATH = "file_url"
        const val EXTRAS_GROUP_OWNER_ADDRESS = "go_host"
        const val EXTRAS_GROUP_OWNER_PORT = "go_port"
    }
}

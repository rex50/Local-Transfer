package com.rex50.android.localtransfer.local.p2p

import android.os.AsyncTask
import com.rex50.android.localtransfer.local.demo.DeviceDetailFragment.Companion.copyFile
import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket

class FileServerAsyncTask(
    private val file: File,
    private val statusCallback: (String) -> Unit,
    private val onComplete: () -> Unit
) : AsyncTask<Void, Void, String?>() {

    override fun doInBackground(vararg params: Void): String? {
        /**
         * Create a server socket.
         */
        val serverSocket = ServerSocket(8888)
        return serverSocket.use {
            /**
             * Wait for client connections. This call blocks until a
             * connection is accepted from a client.
             */
            val client = serverSocket.accept()
            /**
             * If this code is reached, a client has connected and transferred data
             * Save the input stream from the client as a JPEG file
             */
            val f = file
            val dirs = File(f.parent)

            dirs.takeIf { it.doesNotExist() }?.apply {
                mkdirs()
            }
            f.createNewFile()
            val inputstream = client.getInputStream()
            copyFile(inputstream, FileOutputStream(f))
            serverSocket.close()
            f.absolutePath
        }
    }

    private fun File.doesNotExist(): Boolean = !exists()

    /**
     * Start activity that can handle the JPEG image
     */
    override fun onPostExecute(result: String?) {
        result?.run {
            statusCallback("File copied - $result")
            onComplete()
        }
    }
}

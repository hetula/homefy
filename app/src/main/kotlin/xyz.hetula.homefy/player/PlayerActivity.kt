/*
 * MIT License
 *
 * Copyright (c) 2017 Tuomo Heino
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package xyz.hetula.homefy.player

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.util.Log
import xyz.hetula.homefy.HomefyActivity
import xyz.hetula.homefy.R
import xyz.hetula.homefy.service.HomefyService
import java.util.HashMap
import kotlin.collections.ArrayList


/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class PlayerActivity : HomefyActivity() {
    private val mPermissionRequestCode = 0x2231
    private var song: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.hide()
    }

    override fun serviceConnected(service: HomefyService) {
        super.serviceConnected(service)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container, PlayerFragment())
                .commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val song = this.song
        if (requestCode == mPermissionRequestCode && song != null) {
            if (hasExternalStoragePermissions()) {
                saveTheSong(song)
            } else {
                Log.w("PlayerActivity", "No permissions...")
            }
        }
    }

    internal fun download(song: Song?) {
        if (song == null) {
            return
        }
        val askPermissions = ArrayList<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            askPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            askPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (askPermissions.isNotEmpty()) {
            this.song = song
            requestPermissions(askPermissions.toTypedArray(), mPermissionRequestCode)
        } else {
            saveTheSong(song)
        }
    }

    private fun hasExternalStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun saveTheSong(song: Song) {
        if (!isExternalStorageWritable()) {
            Log.e("PlayerActivity", "Can't write to storage!")
            return
        }
        song.getFileType {
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(homefy.getLibrary().getPlayPath(song)))
            val headers = HashMap<String, String>()
            homefy.getProtocol().addAuthHeader(headers)
            request.addRequestHeader("Authorization", headers["Authorization"])
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setVisibleInDownloadsUi(false)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC,
                    "Homefy/" + song.title + "." + it)
            request.setTitle(song.title)
            song.getMimeType { request.setMimeType(it) }
            downloadManager.enqueue(request)
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }


}

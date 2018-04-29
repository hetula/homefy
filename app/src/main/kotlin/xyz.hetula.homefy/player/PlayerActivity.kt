/*
 * Copyright (c) 2018 Tuomo Heino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

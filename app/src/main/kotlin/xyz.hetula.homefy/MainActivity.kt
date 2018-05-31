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

package xyz.hetula.homefy

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import xyz.hetula.homefy.library.LibraryFragment
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.HomefyService
import xyz.hetula.homefy.setup.SetupFragment
import java.util.HashMap
import kotlin.collections.ArrayList

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class MainActivity : HomefyActivity() {
    private val mPermissionRequestCode = 0x2231
    private var song: Song? = null
    private var mSelectTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiCompat.init(BundledEmojiCompatConfig(applicationContext))
        setContentView(R.layout.activity_main)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val bundle = intent.extras
        if(bundle != null) {
            mSelectTab = bundle.getInt(EXTRA_SELECT_TAB, 0)
        }

        val startService = Intent(applicationContext, HomefyService::class.java)
        startService(startService)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, ": Setting select tab to 0")
        mSelectTab = 0
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val bundle = intent?.extras
        if(bundle != null) {
            mSelectTab = bundle.getInt(EXTRA_SELECT_TAB, 0)
        }
    }

    override fun serviceConnected(service: HomefyService) {
        super.serviceConnected(service)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.container,
                        if (service.getLibrary().isLibraryReady()) {
                            LibraryFragment()
                        } else {
                            SetupFragment()
                        })
                .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_shutdown -> {
                doShutdown()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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

    fun getAndClearNewSetupTab(): Int {
        val tab = mSelectTab
        mSelectTab = 0
        return tab
    }

    fun download(song: Song?) {
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
            Log.e("MainActivity", "Can't write to storage!")
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

    private fun doShutdown() {
        Log.d("MainActivity", "Shutting down!")
        stopService(Intent(applicationContext, HomefyService::class.java))
        finishAffinity()
    }

    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_SELECT_TAB = "MainActivity.EXTRA_SELECT_TAB"
    }
}

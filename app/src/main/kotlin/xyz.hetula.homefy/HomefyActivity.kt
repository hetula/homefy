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
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
class HomefyActivity : AppCompatActivity() {
    lateinit var homefy: HomefyService
    private val mHomefyConnection = HomefyConnection { serviceConnected(it) }
    private var mKillReceiver: BroadcastReceiver? = null
    private val mPermissionRequestCode = 0x2231
    private var song: Song? = null
    private var mSelectTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "Creating HomefyActivity")
        super.onCreate(savedInstanceState)
        val filter = IntentFilter(KILL_INTENT)
        mKillReceiver = (object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.v(TAG, "onReceive: " + intent?.action)
                if (intent?.action == KILL_INTENT) {
                    Log.v(TAG, "Finishing Activity!")
                    finishAffinity()
                }
            }
        })
        applicationContext.registerReceiver(mKillReceiver, filter)
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

    override fun onStart() {
        Log.v(TAG, "onStart(): " + javaClass.simpleName)
        super.onStart()
        val intent = Intent(this, HomefyService::class.java)
        if (!bindService(intent, mHomefyConnection,
                        Context.BIND_AUTO_CREATE or Context.BIND_ABOVE_CLIENT)) {
            unbindService(mHomefyConnection)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, ": Setting select tab to 0")
        mSelectTab = 0
    }


    override fun onStop() {
        Log.v(TAG, "onStop(): " + javaClass.simpleName)
        super.onStop()
        unbindService(mHomefyConnection)
    }


    override fun onDestroy() {
        Log.v(TAG, "Destroying HomefyActivity")
        super.onDestroy()
        applicationContext.unregisterReceiver(mKillReceiver)
        mKillReceiver = null
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val bundle = intent?.extras
        if(bundle != null) {
            mSelectTab = bundle.getInt(EXTRA_SELECT_TAB, 0)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val song = this.song
        if (requestCode == mPermissionRequestCode && song != null) {
            if (hasExternalStoragePermissions()) {
                saveTheSong(song)
            } else {
                Log.w(TAG, "No permissions...")
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
            Log.e(TAG, "Can't write to storage!")
            return
        }
        song.getFileType { fileType ->
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(homefy.getLibrary().getPlayPath(song)))
            val headers = HashMap<String, String>()
            homefy.getProtocol().addAuthHeader(headers)
            request.addRequestHeader("Authorization", headers["Authorization"])
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setVisibleInDownloadsUi(false)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC,
                    "Homefy/" + song.title + "." + fileType)
            request.setTitle(song.title)
            song.getMimeType { request.setMimeType(it) }
            downloadManager.enqueue(request)
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    private fun serviceConnected(service: HomefyService) {
        homefy = service
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

    internal class HomefyConnection(private val serviceCallback: (HomefyService) -> Unit) :
            ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.v("HomefyConnection", "HomefyService Disconnected!")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.v("HomefyConnection", "HomefyService Connected!")
            if (service != null) {
                serviceCallback((service as HomefyService.HomefyBinder).getService())
            }
        }

    }

    companion object {
        private const val TAG = "HomefyActivity"
        const val KILL_INTENT = "xyz.hetula.homefy.KILL_INTENT"
        const val EXTRA_SELECT_TAB = "HomefyActivity.EXTRA_SELECT_TAB"
    }
}

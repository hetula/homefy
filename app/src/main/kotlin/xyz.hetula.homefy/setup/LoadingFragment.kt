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

package xyz.hetula.homefy.setup

import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_loading.view.*
import xyz.hetula.homefy.HomefyFragment
import xyz.hetula.homefy.R
import xyz.hetula.homefy.library2.LibraryFragment2
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.protocol.HomefyProtocol
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class LoadingFragment : HomefyFragment() {
    private var mCount: AtomicInteger = AtomicInteger()
    private var mSongs: MutableList<Song> = ArrayList()

    private lateinit var mLoaded: TextView
    private lateinit var mLoadingView: AppCompatImageView

    private var mSongsTotal: Int = 0
    private var mLoadStarted: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val main = inflater.inflate(R.layout.fragment_loading, container, false) as FrameLayout
        mLoaded = main.txt_songs_loaded
        mLoadingView = main.loadingView

        val info = homefy().getProtocol().info
        mSongsTotal = info.databaseSize
        animateLoading()

        initialize(info.databaseId)
        return main
    }

    override fun onDestroyView() {
        mLoadingView.animate().cancel()
        super.onDestroyView()
    }

    private fun animateLoading() {
        mLoadingView.animate()
                .rotationBy(360f)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(1200L)
                .withEndAction(this::animateLoading)
                .start()
    }

    private fun initialize(databaseId: String) {
        mSongs.clear()
        mLoadStarted = SystemClock.elapsedRealtime()
        val serverId = homefy().getProtocol().info.server_id

        homefy().getPlaylists().setBaseLocation(context!!.filesDir.resolve(serverId))
        homefy().getPlaylists().loadPlaylists()
        LoadCacheFile(context!!.filesDir.resolve("cache/").resolve(databaseId)) {
            if (it == null) {
                loadFromNet()
            } else {
                onSongs(it, false)
            }
        }
    }

    private fun loadFromNet() {
        homefy().getProtocol().requestPages(resources.getInteger(R.integer.load_page_song_count),
                this::fetchData,
                { er ->
                    Toast.makeText(context, R.string.setup_connection_error, Toast.LENGTH_LONG).show()
                    Log.e("LoadingFragment", "Connection error! Can't recover!", er.volleyError.cause)
                    // TODO: Try to recover at some point
                    activity!!.finish()
                })
    }

    private fun fetchData(urls: Array<String>) {
        mCount = AtomicInteger(urls.size)
        if (mCount.get() == 0) {
            onSongs(arrayOf())
        } else {
            for (url in urls) {
                homefy().getProtocol().request(
                        url,
                        { onSongs(it) },
                        { _ ->
                            Toast.makeText(context, R.string.setup_connection_error, Toast.LENGTH_LONG).show()
                            onDataRequestFinished(false)
                        },
                        Array<Song>::class.java)
            }
        }
    }

    @Synchronized
    private fun ready(): Boolean {
        return mCount.decrementAndGet() <= 0
    }

    private fun onSongs(songs: Array<Song>, saveLoaded: Boolean = true) {
        mSongs.addAll(Arrays.asList(*songs))
        mLoaded.text = context!!.resources
                .getQuantityString(R.plurals.songs_loaded, mSongs.size, mSongs.size, mSongsTotal)

        onDataRequestFinished(saveLoaded)
    }

    private fun onDataRequestFinished(saveLoaded: Boolean) {
        if (ready()) {
            val time = SystemClock.elapsedRealtime() - mLoadStarted
            Log.d("LoadingFragment", "Songs loaded in $time ms")
            if (saveLoaded) {
                SaveCacheFile(context!!.filesDir, homefy().getProtocol(), mSongs) {
                    initializeHomefy()
                }
            } else {
                initializeHomefy()
            }
        }
    }

    private fun initializeHomefy() {
        val songs = mSongs
        mSongs = ArrayList() // Create new list so old one can't be used here
        // initialize will use given list and does not create new one.
        homefy().getLibrary().initialize(context!!.applicationContext, songs)
        fragmentManager!!
                .beginTransaction()
                .replace(R.id.container, LibraryFragment2())
                .commit()
    }

    private class LoadCacheFile(databaseCache: File,
                                private val resultCb: (Array<Song>?) -> Unit) :
            AsyncTask<File, Void, Array<Song>?>() {

        init {
            execute(databaseCache)
        }

        override fun doInBackground(vararg params: File?): Array<Song>? {
            val databaseCache = params[0]!!
            if (databaseCache.exists()) {
                Log.d("LoadCacheFile", "Loading from cache! $databaseCache")
                return try {
                    val songData = databaseCache.readText()
                    GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                            .create().fromJson<Array<Song>>(songData, Array<Song>::class.java)
                } catch (ex: Exception) {
                    Log.e("LoadCacheFile", "Can't load cache file, reverting to Network!", ex)
                    null
                }
            }
            return null
        }

        override fun onPostExecute(result: Array<Song>?) {
            resultCb(result)
        }
    }

    private class SaveCacheFile(databaseCache: File,
                                private val mProtocol: HomefyProtocol,
                                private val mSongs: List<Song>,
                                private val resultCb: (Unit) -> Unit) :
            AsyncTask<File, Void, Void?>() {

        init {
            execute(databaseCache)
        }

        override fun doInBackground(vararg params: File?): Void? {
            val cacheDir = params[0]!!.resolve("cache/")
            if (!cacheDir.exists()) {
                cacheDir.mkdir()
            } else {
                cacheDir.deleteRecursively()
                cacheDir.mkdir()
            }
            val cacheFile = cacheDir.resolve(mProtocol.info.databaseId)
            Log.d("SAveCacheFile", "Saving to cache! $cacheFile")
            if (cacheFile.exists()) {
                Log.d("SAveCacheFile", "Old cache found! Deleting: ${cacheFile.delete()}")
            }
            cacheFile.writeText(Gson().toJson(mSongs))
            return null
        }

        override fun onPostExecute(result: Void?) {
            resultCb(Unit)
        }
    }
}

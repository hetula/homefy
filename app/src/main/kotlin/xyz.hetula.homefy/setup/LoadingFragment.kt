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
 *
 */

package xyz.hetula.homefy.setup

import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import xyz.hetula.homefy.HomefyFragment
import xyz.hetula.homefy.R
import xyz.hetula.homefy.library.LibraryFragment
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.protocol.HomefyProtocol
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class LoadingFragment : HomefyFragment() {
    private var mCount: AtomicInteger = AtomicInteger()
    private var mSongs: MutableList<Song> = ArrayList()

    private lateinit var mLoaded: TextView

    private var mSongsTotal: Int = 0
    private var mLoadStarted: Long = 0

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val main = inflater!!.inflate(R.layout.fragment_loading, container, false) as FrameLayout
        mLoaded = main.findViewById(R.id.txt_songs_loaded)
        val info = homefy().getProtocol().info
        mSongsTotal = info.databaseSize
        initialize(info.databaseId)
        return main
    }

    private fun initialize(databaseId: String) {
        mSongs.clear()
        mLoadStarted = SystemClock.elapsedRealtime()
        val serverId = homefy().getProtocol().info.server_id

        homefy().getPlaylists().setBaseLocation(context.filesDir.resolve(serverId))
        homefy().getPlaylists().loadPlaylists()
        LoadCacheFile(context.filesDir.resolve("cache/").resolve(databaseId)) {
            if (it == null) {
                loadFromNet()
            } else {
                onSongs(it, false)
            }
        }
    }

    private fun loadFromNet() {
        homefy().getProtocol().requestPages(250,
                this::fetchData,
                { er ->
                    Toast.makeText(context,
                            "Error when Connecting!", Toast.LENGTH_LONG).show()
                    Log.e("LoadingFragment", "Connection error! Can't recover!", er.cause)
                    activity.finish()
                })
    }

    private fun fetchData(urls: Array<String>) {
        mCount = AtomicInteger(urls.size)
        for (url in urls) {
            homefy().getProtocol().request(
                    url,
                    { onSongs(it) },
                    { _ ->
                        Toast.makeText(context,
                                "Error when Connecting!", Toast.LENGTH_LONG).show()
                        onDataRequestFinished(false)
                    },
                    Array<Song>::class.java)
        }
    }

    @Synchronized private fun ready(): Boolean {
        return mCount.decrementAndGet() <= 0
    }

    private fun onSongs(songs: Array<Song>, saveLoaded: Boolean = true) {
        mSongs.addAll(Arrays.asList(*songs))
        mLoaded.text = context.resources
                .getQuantityString(R.plurals.songs_loaded, mSongs.size, mSongs.size, mSongsTotal)

        onDataRequestFinished(saveLoaded)
    }

    private fun onDataRequestFinished(saveLoaded: Boolean) {
        if (ready()) {
            val time = SystemClock.elapsedRealtime() - mLoadStarted
            Log.d("LoadingFragment", "Songs loaded in $time ms")
            if (saveLoaded) {
                SaveCacheFile(context.filesDir, homefy().getProtocol(), mSongs) {
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
        homefy().getLibrary().initialize(context.applicationContext, songs)
        fragmentManager
                .beginTransaction()
                .replace(R.id.container, LibraryFragment())
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

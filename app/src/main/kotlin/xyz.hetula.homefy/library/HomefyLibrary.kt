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

package xyz.hetula.homefy.library

import android.os.AsyncTask
import android.os.SystemClock
import android.util.Log
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.Homefy
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Library Class that implements storing all songs
 * from Server.
 * Caches all artists and albums and creates song lists
 * for each of them ready.
 * All of this is held in memory so using very big
 * Homefy Library will cause big memory usage.
 *
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class HomefyLibrary {
    private val mSearchExecutor = Executors.newCachedThreadPool()
    private var mSongDatabase: MutableMap<String, Song>? = null
    private val mArtistCache = HashMap<String, ArrayList<Song>>()
    private val mAlbumCache = HashMap<String, ArrayList<Song>>()
    private var mMusic: MutableList<Song> = Collections.emptyList()
    private var mAlbums: MutableList<String> = Collections.emptyList()
    private var mArtists: MutableList<String> = Collections.emptyList()

    private var mSearchTask: AsyncTask<SearchRequest, Void, List<Song>>? = null

    fun initialize(music: List<Song>) {
        Log.d(TAG, "Initializing with ${music.size} songs!")
        val start = SystemClock.elapsedRealtime()
        val size = music.size
        mMusic = ArrayList(sanitizeMusic(music))
        Log.d(TAG, "Sanitized from $size to ${mMusic.size}")

        Collections.sort(mMusic)

        mSongDatabase = HashMap((mMusic.size * 1.25).toInt())

        for (song in mMusic) {
            mSongDatabase!!.put(song.id, song)
            createAndAdd(mArtistCache, song.artist, song)
            createAndAdd(mAlbumCache, song.album, song)
        }

        // Init lists
        mAlbums = ArrayList(mAlbumCache.keys)
        mArtists = ArrayList(mArtistCache.keys)

        // Sort
        Collections.sort(mAlbums)
        Collections.sort(mArtists)
        val time = SystemClock.elapsedRealtime() - start
        Log.d("HomefyLibrary", "Library initialized in $time ms")
    }

    fun release() {
        mSearchExecutor.shutdown()
        try {
            mSearchExecutor.awaitTermination(3, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            Log.e(TAG, "Interrupt when waiting.", ex)
        }
        mSearchExecutor.shutdownNow()

        mSongDatabase?.clear()
        mArtistCache.clear()
        mAlbumCache.clear()
        mAlbums.clear()
        mArtists.clear()
    }

    private fun sanitizeMusic(music: List<Song>): List<Song> {
        // WMA filter, low bitrate filter(0 can be unknown)
        return music
                .filter { !it.type.startsWith("ASF") }
                .filter { it.bitrate <= 0 || it.bitrate >= 128 }
    }

    private fun createAndAdd(cache: MutableMap<String, ArrayList<Song>>, key: String, song: Song) {
        var list: ArrayList<Song>? = cache[key]
        if (list == null) {
            list = ArrayList()
            cache.put(key, list)
        }
        list.add(song)
    }

    val songs: List<Song>
        get() = mMusic

    val artists: List<String>
        get() = mArtists

    val albums: List<String>
        get() = mAlbums

    fun getArtistSongs(artist: String): List<Song> {
        return getFromCache(mArtistCache, artist)
    }

    fun getAlbumSongs(album: String): List<Song> {
        return getFromCache(mAlbumCache, album)
    }

    private fun getFromCache(cache: Map<String, List<Song>>, key: String): List<Song> {
        val songs = cache[key]
        return songs ?: emptyList()
    }

    fun getPlayPath(song: Song): String {
        return Homefy.protocol().server + "/play/" + song.id
    }

    @Synchronized
    fun search(search: String, type: SearchType, callback: (List<Song>) -> Unit) {
        mSearchTask?.cancel(true)
        mSearchTask = SearchTask(mMusic, mSearchExecutor, callback)
        mSearchTask?.execute(SearchRequest(search, type))
    }

    data class SearchRequest(val search: String, val type: SearchType)

    private class SearchTask(val music: List<Song>, val executor: Executor,
                             val callback: (List<Song>) -> Unit) :
            AsyncTask<SearchRequest, Void, List<Song>>() {

        override fun doInBackground(vararg params: SearchRequest?): List<Song> {
            val req = params[0]!!
            val start = SystemClock.elapsedRealtime()
            val result = search(req.search.toLowerCase(), req.type)
            Log.d(TAG, "Search done in: ${(SystemClock.elapsedRealtime() - start)} ms")
            return result
        }

        override fun onPostExecute(result: List<Song>?) {
            if (result != null) {
                callback(result)
            }
        }

        private fun search(search: String, type: SearchType): List<Song> {
            val results = ArrayList<Song>()
            val workers = getThreadCount(music.size)

            if (workers == 1) {
                processAll(results, search, type)
            } else {
                lickitySplit(workers, results, search, type)
            }
            return results
        }

        private fun processAll(results: MutableList<Song>, search: String, type: SearchType) {
            SearchWorker(music, search, type, this::isCancelled) {
                results.addAll(it)
            }.run()
        }

        private fun lickitySplit(workers: Int,
                                 results: MutableList<Song>,
                                 search: String, type: SearchType) {
            val reqSize = music.size / workers
            val latch = CountDownLatch(workers)
            var lastIndex = 0
            for (i in 0 until workers - 1) {
                lastIndex = i * reqSize + reqSize
                val list = music.subList(i * reqSize, lastIndex)
                executor.execute(SearchWorker(list, search, type, this::isCancelled, latch) {
                    synchronized(results) {
                        results.addAll(it)
                    }
                })
            }
            val list = music.subList(lastIndex, music.size)
            SearchWorker(list, search, type, this::isCancelled, latch) {
                synchronized(results) {
                    results.addAll(it)
                }
            }.run()

            latch.await()
            if (isCancelled) return
            results.sort()
        }

        private fun getThreadCount(size: Int): Int {
            if (size < 500) {
                return 1
            }
            if (size < 1000) {
                return 2
            }
            return Math.max(3, Runtime.getRuntime().availableProcessors())
        }
    }

    private class SearchWorker(val searchList: List<Song>,
                               val search: String,
                               val type: SearchType,
                               val cancel: () -> Boolean,
                               val latch: CountDownLatch? = null,
                               val callback: (List<Song>) -> Unit) : Runnable {
        override fun run() {
            val results = ArrayList<Song>()
            for (song in searchList) {
                if (cancel()) {
                    latch?.countDown()
                    return
                }
                if (filter(song, search, type)) {
                    results.add(song)
                }
            }
            callback(results)
            latch?.countDown()
        }

        private fun filter(song: Song, search: String, type: SearchType): Boolean {
            return when (type) {
                SearchType.TITLE -> contains(song.title, search)
                SearchType.ALBUM -> contains(song.album, search)
                SearchType.ARTIST -> contains(song.artist, search)
                SearchType.GENRE -> contains(song.genre, search)
                else -> contains(song.title, search) ||
                        contains(song.album, search) ||
                        contains(song.artist, search)
            }
        }

        private fun contains(value: String, search: String): Boolean {
            if (search.length > value.length) {
                return false
            }
            return value.toLowerCase().contains(search)
        }
    }

    companion object {
        val TAG = "HomefyLibrary"
    }
}

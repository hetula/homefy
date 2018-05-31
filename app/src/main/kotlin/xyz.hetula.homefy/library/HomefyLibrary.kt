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

package xyz.hetula.homefy.library

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.HomefyService
import xyz.hetula.homefy.service.protocol.HomefyProtocol
import java.util.*
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
class HomefyLibrary(private val protocol: HomefyProtocol) {
    private val mSearchExecutor = Executors.newCachedThreadPool()
    private var mSongDatabase: MutableMap<String, Song>? = null
    private val mArtistCache = HashMap<String, ArrayList<Song>>()
    private val mAlbumCache = HashMap<String, ArrayList<Song>>()
    private var mMusic: MutableList<Song> = Collections.emptyList()
    private var mAlbums: MutableList<String> = Collections.emptyList()
    private var mArtists: MutableList<String> = Collections.emptyList()

    private var mReady = false

    fun initialize(context: Context, music: List<Song>) {
        Log.d(TAG, "Initializing with ${music.size} songs!")
        val start = SystemClock.elapsedRealtime()
        val size = music.size
        mMusic = ArrayList(sanitizeMusic(music))
        Log.d(TAG, "Sanitized from $size to ${mMusic.size}")

        mMusic.sort()

        mSongDatabase = HashMap((mMusic.size * 1.25).toInt())

        for (song in mMusic) {
            mSongDatabase!![song.id] = song
            createAndAdd(mArtistCache, song.artist, song)
            createAndAdd(mAlbumCache, song.album, song)
        }

        // Init lists
        mAlbums = ArrayList(mAlbumCache.keys)
        mArtists = ArrayList(mArtistCache.keys)

        // Sort
        mAlbums.sort()
        mArtists.sort()
        val time = SystemClock.elapsedRealtime() - start
        mReady = true
        val completeIntent = Intent(context, HomefyService::class.java)
        completeIntent.action = HomefyService.INIT_COMPLETE
        context.startService(completeIntent)
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
        mReady = false
    }

    fun isLibraryReady() = mReady

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
            cache[key] = list
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
        return protocol.server + "/play/" + song.id
    }

    fun getSongById(songId: String): Song? = mSongDatabase?.get(songId)

    companion object {
        const val TAG = "HomefyLibrary"
    }
}

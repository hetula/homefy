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

import android.util.Log
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.Homefy
import java.util.*

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
    private var mSongDatabase: MutableMap<String, Song>? = null
    private val mArtistCache = HashMap<String, ArrayList<Song>>()
    private val mAlbumCache = HashMap<String, ArrayList<Song>>()
    private var mMusic: MutableList<Song>? = null
    private var mAlbums: MutableList<String>? = null
    private var mArtists: MutableList<String>? = null

    fun initialize(music: MutableList<Song>) {
        Log.d("HomefyLibrary", "Initializing with " + music.size + " songs!")
        val start = System.currentTimeMillis()
        mMusic = music
        Collections.sort(mMusic!!)

        // Take account load factor, probably too low but can be optimized later
        // TODO Check correct load factor
        mSongDatabase = HashMap<String, Song>((music.size * 1.1).toInt())

        for (song in music) {
            mSongDatabase!!.put(song.id, song)
            createAndAdd(mArtistCache, song.artist, song)
            createAndAdd(mAlbumCache, song.album, song)
        }

        // Init lists
        mAlbums = ArrayList(mAlbumCache.keys)
        mArtists = ArrayList(mArtistCache.keys)

        // Sort
        Collections.sort(mAlbums!!)
        Collections.sort(mArtists!!)
        val time = System.currentTimeMillis() - start
        Log.d("HomefyLibrary", "Library initialized in $time ms")
    }

    fun release() {
        mSongDatabase!!.clear()
        mArtistCache.clear()
        mAlbumCache.clear()
        mMusic!!.clear()
        mAlbums!!.clear()
        mArtists!!.clear()

    }

    private fun createAndAdd(cache: MutableMap<String, ArrayList<Song>>, key: String, song: Song) {
        var list: ArrayList<Song>? = cache[key]
        if (list == null) {
            list = ArrayList<Song>()
            cache.put(key, list)
        }
        list.add(song)
    }

    val songs: List<Song>
        get() = mMusic!!

    val artists: List<String>
        get() = mArtists!!

    val albums: List<String>
        get() = mAlbums!!

    fun getArtistSongs(artist: String): List<Song> {
        return getFromCache(mArtistCache, artist)
    }

    fun getAlbumSongs(album: String): List<Song> {
        return getFromCache(mAlbumCache, album)
    }

    private fun getFromCache(cache: Map<String, List<Song>>, key: String): List<Song> {
        val songs = cache[key]
        return songs ?: emptyList<Song>()
    }

    fun getSong(id: String): Song? {
        return mSongDatabase!![id]
    }

    fun getPlayPath(song: Song): String {
        return Homefy.protocol().server + "/play/" + song.id
    }
}

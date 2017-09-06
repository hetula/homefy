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

package xyz.hetula.homefy.playlist

import android.util.Log
import com.google.gson.Gson
import xyz.hetula.homefy.Utils
import xyz.hetula.homefy.player.Song
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class HomefyPlaylist {
    private val PLAYLIST_DIR = "playlists/"
    private val mPlaylists = HashMap<String, Playlist>()
    val favorites = Playlist("favorites", "Favorites", favs = true)
    internal var baseLocation: File? = null

    fun setBaseLocation(base: File) {
        baseLocation = base
        baseLocation?.mkdir()
    }

    fun isFavorite(song: Song): Boolean {
        return favorites.contains(song)
    }

    fun createPlaylist(name: String): Playlist {
        val pl = Playlist(Utils.randomId(), if (name.isBlank()) "Empty" else name)
        mPlaylists[pl.id] = pl
        pl.create()
        return pl
    }

    fun loadPlaylists() {
        val base = baseLocation ?: return
        val gson = Gson()
        val favs = base.resolve("favorites.json")
        loadPlaylist(gson, favs) { favList ->
            favorites.addAll(favList.songs)
        }
        val playlistFolder = base.resolve(PLAYLIST_DIR)
        val playlists = playlistFolder.list({ _, name -> name.endsWith(".json") })
        if (playlists == null || playlists.isEmpty()) {
            Log.e("HomefyPlaylist", "No playlists found! $playlistFolder")
            return
        }
        for (playlist in playlists) {
            loadPlaylist(gson, playlistFolder.resolve(playlist)) {
                mPlaylists[it.id] = it
            }
        }
    }

    private fun loadPlaylist(gson: Gson, plFile: File, loadCb: (Playlist) -> Unit) {
        var read: BufferedReader? = null
        try {
            read = BufferedReader(FileReader(plFile))
            loadCb(gson.fromJson<Playlist>(read, Playlist::class.java))
        } catch (ex: Exception) {
            Log.e("HomefyPlaylist", "Exception when loading $plFile", ex)
        } finally {
            read?.close()
        }
    }

    operator fun get(key: String): Playlist? {
        return mPlaylists[key]
    }

    fun getAllPlaylists(): Set<Playlist> {
        return HashSet(mPlaylists.values)
    }
}

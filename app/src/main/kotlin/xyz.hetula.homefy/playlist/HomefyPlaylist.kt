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

package xyz.hetula.homefy.playlist

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import xyz.hetula.homefy.R
import xyz.hetula.homefy.Utils
import xyz.hetula.homefy.player.Song
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
open class HomefyPlaylist(private val mContext: Context) {
    private val playlistDirectory = "playlists/"
    private val mPlaylists = HashMap<String, Playlist>()
    val favorites = Playlist(Playlist.FAVORITES_PLAYLIST_ID, mContext.getString(R.string.library_favs), favs = true)
    internal lateinit var baseLocation: File

    fun setBaseLocation(base: File) {
        baseLocation = base
        baseLocation.mkdir()
    }

    fun isFavorite(song: Song): Boolean {
        return favorites.contains(song)
    }

    fun createPlaylist(name: String): Playlist {
        val pl = Playlist(Utils.randomId(), if (name.isBlank()) "Empty" else name)
        mPlaylists[pl.id] = pl
        pl.create(mContext, this)
        return pl
    }

    fun loadPlaylists() {
        val base = baseLocation
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        val favs = base.resolve("favorites.json")
        loadPlaylist(gson, favs) { favList ->
            favorites.addAll(favList.songs)
        }
        val playlistFolder = base.resolve(playlistDirectory)
        val playlists = playlistFolder.list({ _, name -> name.endsWith(".json") })
        if (playlists == null || playlists.isEmpty()) {
            Log.i("HomefyPlaylist", "No playlists found! $playlistFolder")
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
        if (!plFile.exists()) {
            Log.w("HomefyPlaylist", "No Playlist file found! ${plFile.absolutePath}")
            return
        }
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

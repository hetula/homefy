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

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import xyz.hetula.homefy.player.Song
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
data class Playlist(@Expose val id: String,
                    @Expose val name: String,
                    @Expose val songs: MutableList<Song> = ArrayList(),
                    @Expose internal val favs: Boolean = false) {

    fun contains(song: Song): Boolean {
        return songs.contains(song)
    }

    fun toggle(homefyPlaylist: HomefyPlaylist, song: Song) {
        if (!songs.remove(song)) {
            songs.add(song)
        }
        save(homefyPlaylist.baseLocation)
    }

    fun add(homefyPlaylist: HomefyPlaylist, song: Song) {
        if (!contains(song)) {
            songs.add(song)
            save(homefyPlaylist.baseLocation)
        }
    }

    fun remove(homefyPlaylist: HomefyPlaylist, song: Song) {
        if (songs.remove(song)) {
            save(homefyPlaylist.baseLocation)
        }
    }

    fun create(homefyPlaylist: HomefyPlaylist) {
        save(homefyPlaylist.baseLocation)
    }

    internal fun addAll(songs: List<Song>) {
        this.songs.addAll(songs)
    }

    private fun save(base: File) {
        val fileName = resolveFile(base)
        var io: BufferedWriter? = null
        try {
            io = BufferedWriter(FileWriter(fileName))
            GSON.toJson(this, io)
            Log.d("Playlist", "Saved: $fileName")
        } catch (ioEx: IOException) {
            Log.e("Playlist", "Saving: $fileName", ioEx)
        } finally {
            io?.close()
        }
    }

    private fun resolveFile(base: File): File {
        if (favs) {
            return base.resolve("$id.json")
        }
        val pls = base.resolve("playlists/")
        pls.mkdir()
        return pls.resolve("$id.json")
    }

    override fun toString(): String {
        return name
    }

    companion object {
        private val GSON = Gson()
    }
}

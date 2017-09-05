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
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.Homefy
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
data class Playlist(val id: String, val name: String, val songs: MutableList<Song> = ArrayList(), internal val favs: Boolean = false) {

    fun contains(song: Song): Boolean {
        return songs.contains(song)
    }

    fun toggle(song: Song) {
        if (songs.remove(song)) {
            save()
            return
        }
        songs.add(song)
        save()
    }

    internal fun addAll(songs: List<Song>) {
        this.songs.addAll(songs)
    }

    fun create() {
        save()
    }

    private fun save() {
        val base = Homefy.playlist().baseLocation ?: return
        val fileName = resolveFile(base)
        var io: BufferedWriter? = null
        try {
            io = BufferedWriter(FileWriter(fileName))
            GSON.toJson(this, io)
            Log.d("Playlist", "Saved: $name")
        } catch (ioEx: IOException) {
            Log.e("Playlist", "Saving: $name", ioEx)
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

    companion object {
        private val GSON = Gson()
    }
}

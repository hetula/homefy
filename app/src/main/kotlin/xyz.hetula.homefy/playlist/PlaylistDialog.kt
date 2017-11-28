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

import android.app.AlertDialog
import android.content.Context
import android.widget.ArrayAdapter
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.Song

object PlaylistDialog {

    fun addToPlaylist(context: Context, song: Song, homefyPlaylists: HomefyPlaylist,
                      onSuccess: (Unit) -> Unit) {
        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(R.string.playlist_dialog_title)


        val playlists = getPlaylists(song, homefyPlaylists)
        val text: Int
        text = if (playlists.isEmpty()) {
            dialog.setMessage(R.string.playlist_dialog_no_playlists)
            android.R.string.ok
        } else {
            dialog.setAdapter(ArrayAdapter<Playlist>(context, android.R.layout.simple_list_item_1,
                    playlists)) { dlg, index ->
                val selected = playlists[index]
                selected.add(homefyPlaylists, song)
                dlg.dismiss()
                onSuccess(Unit)
            }
            android.R.string.cancel
        }
        dialog.setNegativeButton(text) { dlg, _ -> dlg.dismiss() }
        dialog.show()
    }

    private fun getPlaylists(song: Song, homefyPlaylists: HomefyPlaylist): List<Playlist> {
        val pls = homefyPlaylists.getAllPlaylists()
        val playlists = ArrayList<Playlist>()
        pls.filterNotTo(playlists) { it.contains(song) }
        return playlists
    }
}

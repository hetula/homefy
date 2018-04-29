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

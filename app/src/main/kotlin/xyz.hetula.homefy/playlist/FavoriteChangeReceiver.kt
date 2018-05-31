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
import android.content.Intent
import xyz.hetula.homefy.EasyBroadcastReceiver
import xyz.hetula.homefy.library.HomefyLibrary
import xyz.hetula.homefy.player.Song

class FavoriteChangeReceiver(private val library: HomefyLibrary,
                             private val onFavoriteChange: (Song) -> Unit) :
        EasyBroadcastReceiver(Playlist.ACTION_PLAYLIST_SONG_ADD, Playlist.ACTION_PLAYLIST_SONG_REMOVED) {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        val playlistId = intent.getStringExtra(Playlist.EXTRA_PLAYLIST_ID)
        if (playlistId != Playlist.FAVORITES_PLAYLIST_ID) {
            return
        }
        val songId = intent.getStringExtra(Playlist.EXTRA_SONG_ID)
        val song = library.getSongById(songId) ?: return
        onFavoriteChange(song)
    }

}

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
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import xyz.hetula.homefy.R
import xyz.hetula.homefy.parseSeconds
import xyz.hetula.homefy.player.HomefyPlayer
import xyz.hetula.homefy.player.PlayerActivity
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.playlist.HomefyPlaylist
import xyz.hetula.homefy.playlist.Playlist
import xyz.hetula.homefy.playlist.PlaylistDialog
import java.util.*

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class SongAdapter(songs: List<Song>,
                  private val mPlayer: HomefyPlayer,
                  private val mPlaylists: HomefyPlaylist,
                  private val onFav: ((SongAdapter, Song) -> Unit)? = null,
                  private val playlist: Playlist? = null) :
        RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    private val mSongs: ArrayList<Song> = ArrayList(songs)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val songView = inflater.inflate(R.layout.list_item_song, parent, false)
        val svh = SongViewHolder(this, songView)
        songView.setOnLongClickListener { _ -> svh.onLongClick() }
        return svh
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = mSongs[position]
        holder.song = song
        holder.playlist = playlist
        if (song.track < 1) {
            holder.txtTrackTitle.text = song.title
        } else {
            holder.txtTrackTitle.text = String.format(Locale.getDefault(), "%d - %s", song.track, song.title)
        }
        holder.txtArtistAlbum.text = String.format(Locale.getDefault(), "%s - %s", song.artist, song.album)
        holder.txtLength.text = song.length.parseSeconds()
        holder.itemView.setOnClickListener { v ->
            mPlayer.play(song, mSongs)
            openPlayer(v.context)
        }
        if (mPlaylists.isFavorite(song)) {
            holder.btnSongFav.setImageResource(R.drawable.ic_favorite)
        } else {
            holder.btnSongFav.setImageResource(R.drawable.ic_not_favorite)
        }
        holder.btnSongFav.setOnClickListener {
            mPlaylists.favorites.toggle(mPlaylists, song)
            notifyItemChanged(position)
            onFav?.invoke(this, song)
        }
    }

    override fun getItemCount(): Int {
        return mSongs.size
    }

    fun setSongs(songs: List<Song>) {
        mSongs.clear()
        mSongs.addAll(songs)
        notifyDataSetChanged()
    }

    fun remove(song: Song) {
        val pos = mSongs.indexOf(song)
        if (pos == -1) return
        mSongs.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun add(song: Song) {
        if (mSongs.contains(song)) return
        val pos = mSongs.size
        mSongs.add(song)
        notifyItemInserted(pos)
    }

    private fun openPlayer(context: Context) {
        val intent = Intent(context, PlayerActivity::class.java)
        context.startActivity(intent)
    }

    class SongViewHolder(private val songAdapter: SongAdapter,
                         itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTrackTitle: TextView = itemView.findViewById(R.id.song_track_title)
        val txtArtistAlbum: TextView = itemView.findViewById(R.id.song_artist_album)
        val txtLength: TextView = itemView.findViewById(R.id.song_length)
        val btnSongFav: ImageButton = itemView.findViewById(R.id.song_favorite)

        var playlist: Playlist? = null
        var song: Song? = null

        fun onLongClick(): Boolean {
            val song = this.song ?: return false
            val pops = PopupMenu(itemView.context, itemView)
            pops.menu.add(Menu.NONE, 0, 0, R.string.menu_song_queue)
            pops.menu.add(Menu.NONE, 1, 1, getPlaylistString())
            pops.setOnMenuItemClickListener { click(it.itemId, song) }
            pops.show()
            return true
        }

        @StringRes
        private fun getPlaylistString(): Int {
            return if (playlist == null) R.string.menu_song_add_to_playlist
            else R.string.menu_song_remove_from_playlist
        }

        private fun click(id: Int, song: Song): Boolean {
            when (id) {
                0 -> songAdapter.mPlayer.queue(song)
                1 -> addToPlaylist()
            }
            return true
        }

        private fun addToPlaylist() {
            val song = this.song ?: return
            val playlist = this.playlist
            if (playlist == null) {
                PlaylistDialog.addToPlaylist(itemView.context, song, songAdapter.mPlaylists) {
                    Snackbar.make(itemView, R.string.playlist_dialog_added,
                            Snackbar.LENGTH_SHORT).show()
                }
            } else {
                playlist.remove(songAdapter.mPlaylists, song)
                songAdapter.mSongs.removeAt(adapterPosition)
                songAdapter.notifyItemRemoved(adapterPosition)
                Snackbar.make(itemView, R.string.playlist_dialog_removed,
                        Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}

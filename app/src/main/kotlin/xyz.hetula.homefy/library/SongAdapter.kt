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

import android.content.Context
import android.content.Intent
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import xyz.hetula.homefy.R
import xyz.hetula.homefy.Utils
import xyz.hetula.homefy.player.PlayerActivity
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.playlist.Playlist
import xyz.hetula.homefy.playlist.PlaylistDialog
import xyz.hetula.homefy.service.Homefy
import java.util.*

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class SongAdapter(songs: List<Song>,
                  private val onFav: ((SongAdapter, Song) -> Unit)? = null,
                  private val playlist: Playlist? = null) :
        RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    private val mSongs: ArrayList<Song> = ArrayList(songs)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val songView = inflater.inflate(R.layout.list_item_song, parent, false)
        val svh = SongViewHolder(this, songView)
        songView.setOnLongClickListener(svh::onLong)
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
        holder.txtLength.text = Utils.parseSeconds(song.length)
        holder.itemView.setOnClickListener { v ->
            Homefy.player().play(song, mSongs)
            openPlayer(v.context)
        }
        if (Homefy.playlist().isFavorite(song)) {
            holder.btnSongFav.setImageResource(R.drawable.ic_favorite)
        } else {
            holder.btnSongFav.setImageResource(R.drawable.ic_not_favorite)
        }
        holder.btnSongFav.setOnClickListener {
            Homefy.playlist().favorites.toggle(song)
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

    class SongViewHolder(val songAdapter: SongAdapter,
                         itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTrackTitle: TextView = itemView.findViewById(R.id.song_track_title)
        val txtArtistAlbum: TextView = itemView.findViewById(R.id.song_artist_album)
        val txtLength: TextView = itemView.findViewById(R.id.song_length)
        val btnSongFav: ImageButton = itemView.findViewById(R.id.song_favorite)

        var playlist: Playlist? = null
        var song: Song? = null

        fun onLong(v: View?): Boolean {
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
                0 -> Homefy.player().queue(song)
                1 -> addToPlaylist()
            }
            return true
        }

        private fun addToPlaylist() {
            val song = this.song ?: return
            val playlist = this.playlist
            if (playlist == null) {
                PlaylistDialog.addToPlaylist(itemView.context, song) {
                    Snackbar.make(itemView, R.string.playlist_dialog_added,
                            Snackbar.LENGTH_SHORT).show()
                }
            } else {
                playlist.remove(song)
                songAdapter.mSongs.removeAt(adapterPosition)
                songAdapter.notifyItemRemoved(adapterPosition)
                Snackbar.make(itemView, R.string.playlist_dialog_removed,
                        Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}

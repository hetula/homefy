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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.ArrayList
import java.util.Locale

import xyz.hetula.homefy.R
import xyz.hetula.homefy.Utils
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.Homefy

class SongAdapter(songs: List<Song>) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    private val mSongs: List<Song>

    init {
        this.mSongs = ArrayList(songs)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val songView = inflater.inflate(R.layout.view_song, parent, false)
        return SongViewHolder(songView)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = mSongs[position]
        if (song.track < 0) {
            holder.txtTrackTitle.text = song.title
        } else {
            holder.txtTrackTitle.text = String.format(Locale.getDefault(), "%d - %s", song.track, song.title)
        }
        holder.txtArtistAlbum.text = String.format(Locale.getDefault(), "%s - %s", song.artist, song.album)
        holder.txtLength.text = Utils.parseSeconds(song.length)
        holder.itemView.setOnClickListener { v -> Homefy.player().play(song, mSongs) }
    }

    override fun getItemCount(): Int {
        return mSongs.size
    }

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTrackTitle: TextView = itemView.findViewById(R.id.song_track_title) as TextView
        val txtArtistAlbum: TextView = itemView.findViewById(R.id.song_artist_album) as TextView
        val txtLength: TextView = itemView.findViewById(R.id.song_length) as TextView
    }
}

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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import kotlinx.android.synthetic.main.list_item_playlist.view.*
import xyz.hetula.homefy.R
import xyz.hetula.homefy.playlist.Playlist

class PlaylistAdapter(playlists: List<Playlist>,
                      private val onClick: (Playlist) -> Unit) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    private val mPlaylists: SortedList<Playlist> = SortedList(Playlist::class.java, PlaylistSorter(this))

    init {
        mPlaylists.addAll(playlists)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PlaylistViewHolder(inflater.inflate(R.layout.list_item_playlist, parent, false))
    }

    override fun getItemCount() = mPlaylists.size()

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = mPlaylists[position]
        holder.playlistViewBase.setOnClickListener {
            onClick(playlist)
        }
        holder.playlistTitle.text = playlist.name
        val songCount = playlist.songs.size
        holder.songCount.text = holder.itemView.context.resources.getQuantityString(R.plurals.song_count, songCount, songCount)
    }

    fun addPlaylist(playlist: Playlist) {
        mPlaylists.add(playlist)
    }

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playlistViewBase: ConstraintLayout = view.playlistViewBase
        val playlistTitle: TextView = view.playlistTitle
        val songCount: TextView = view.songCount
    }

    class PlaylistSorter(adapter: PlaylistAdapter) : SortedListAdapterCallback<Playlist>(adapter) {
        override fun areItemsTheSame(item1: Playlist?, item2: Playlist?): Boolean {
            return item1?.id == item2?.id
        }

        override fun compare(o1: Playlist?, o2: Playlist?): Int {
            if (o1 == null || o2 == null) {
                return 1
            }
            if (o1.favs) {
                return -1
            }
            if (o2.favs) {
                return 1
            }
            return o1.name.compareTo(o2.name)
        }

        override fun areContentsTheSame(oldItem: Playlist?, newItem: Playlist?): Boolean {
            return false
        }

    }
}

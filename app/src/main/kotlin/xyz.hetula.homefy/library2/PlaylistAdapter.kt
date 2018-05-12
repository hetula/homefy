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

package xyz.hetula.homefy.library2

import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_playlist2.view.*
import xyz.hetula.homefy.R
import xyz.hetula.homefy.playlist.Playlist

class PlaylistAdapter(private val mPlaylists: List<Playlist>,
                      private val onClick: (Playlist) -> Unit) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PlaylistViewHolder(inflater.inflate(R.layout.list_item_playlist2, parent, false))
    }

    override fun getItemCount() = mPlaylists.size

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = mPlaylists[position]
        holder.playlistViewBase.setOnClickListener {
            onClick(playlist)
        }
        holder.playlistTitle.text = playlist.name
        val songCount = playlist.songs.size
        holder.songCount.text = holder.itemView.context.resources.getQuantityString(R.plurals.song_count, songCount, songCount)
    }

    class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playlistViewBase: ConstraintLayout = view.playlistViewBase
        val playlistTitle: TextView = view.playlistTitle
        val songCount: TextView = view.songCount
    }
}

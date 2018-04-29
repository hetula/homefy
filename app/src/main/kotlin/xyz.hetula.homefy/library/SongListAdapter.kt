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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.HomefyPlayer
import xyz.hetula.homefy.player.Song

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
internal class SongListAdapter(names: List<String>,
                               private val mPlayer: HomefyPlayer,
                               private val mSongFetch: (String) -> List<Song>,
                               private val mClick: (String) -> Unit) :
        RecyclerView.Adapter<SongListAdapter.SongListViewHolder>() {

    private val mNameList: List<String>

    init {
        this.mNameList = ArrayList(names)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val songView = inflater.inflate(R.layout.list_item_songlist, parent, false)
        val slvh = SongListViewHolder(this, songView)
        songView.setOnLongClickListener { _ -> slvh.onLongClick() }
        return slvh
    }

    override fun onBindViewHolder(holder: SongListViewHolder, position: Int) {
        val info = mNameList[position]
        holder.txtMainInfo.text = info
        val songs = mSongFetch(info)
        val count = songs.size
        val ctx = holder.itemView.context
        val str = ctx.resources.getQuantityString(R.plurals.song_count, count, count)
        holder.txtMoreInfo.text = str
        holder.itemView.setOnClickListener { mClick(info) }
        holder.songs = songs
    }

    override fun getItemCount(): Int {
        return mNameList.size
    }

    class SongListViewHolder(val songListAdapter: SongListAdapter, itemView: View) :
            RecyclerView.ViewHolder(itemView) {
        val txtMainInfo: TextView = itemView.findViewById(R.id.txt_main_info)
        val txtMoreInfo: TextView = itemView.findViewById(R.id.txt_more_info)

        var songs: List<Song> = ArrayList()

        fun onLongClick(): Boolean {
            val songs = this.songs
            if (songs.isEmpty()) return false
            val pops = PopupMenu(itemView.context, itemView)
            pops.menu.add(Menu.NONE, PLAY_ALL_ID, 0, R.string.menu_song_play_all)
            pops.menu.add(Menu.NONE, QUEUE_ALL_ID, 1, R.string.menu_song_queue)
            pops.setOnMenuItemClickListener { click(it.itemId, songs) }
            pops.show()
            return true
        }

        private fun click(id: Int, songs: List<Song>): Boolean {
            when (id) {
                PLAY_ALL_ID -> songListAdapter.mPlayer.play(songs.first(), ArrayList(songs))
                QUEUE_ALL_ID -> songListAdapter.mPlayer.queue(songs)
            }
            return true
        }

        companion object {
            private val PLAY_ALL_ID = 0
            private val QUEUE_ALL_ID = 1
        }

    }
}

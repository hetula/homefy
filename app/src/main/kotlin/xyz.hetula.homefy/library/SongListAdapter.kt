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
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.Homefy

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
internal class SongListAdapter(names: List<String>,
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
        val slvh = SongListViewHolder(songView)
        songView.setOnLongClickListener(slvh::onLong)
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

    class SongListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMainInfo: TextView = itemView.findViewById(R.id.txt_main_info)
        val txtMoreInfo: TextView = itemView.findViewById(R.id.txt_more_info)

        var songs: List<Song> = ArrayList()
            get
            set

        fun onLong(v: View?): Boolean {
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
                PLAY_ALL_ID -> Homefy.player().play(songs.first(), ArrayList(songs))
                QUEUE_ALL_ID -> Homefy.player().queue(songs)
            }
            return true
        }

        companion object {
            private val PLAY_ALL_ID = 0
            private val QUEUE_ALL_ID = 1
        }

    }
}

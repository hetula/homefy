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
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import kotlinx.android.synthetic.main.list_item_album.view.*
import xyz.hetula.homefy.R
import xyz.hetula.homefy.forEach
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.HomefyService

class AlbumAdapter(private val originalAlbums: List<String>,
                   private val homefy: HomefyService,
                   private val onAlbumClick: (String) -> Unit) :
        RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>(), SearchableAdapter<String> {

    override val mItems = SortedList<String>(String::class.java, AlbumSorter(this))
    override var mLastSearch: String = ""
    override var mCurrentSearchTask: SearchTask<String>? = null

    init {
        mItems.addAll(originalAlbums)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AlbumViewHolder(inflater.inflate(R.layout.list_item_album, parent, false))
    }

    override fun getOriginalItems() = originalAlbums

    override fun getItemCount() = mItems.size()

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = mItems[position]
        val songs = homefy.getLibrary().getAlbumSongs(album)
        holder.albumTitle.text = album
        holder.artistNames.text = songs.map { it.artist }.distinct().reduce { allArtists, artist ->
            "$allArtists, $artist"
        }
        holder.albumBase.setOnClickListener {
            onAlbumClick(album)
        }
    }

    override fun searchFilter(item: String, search: String) = !item.contains(search, true)

    override fun playAll() {
        if(mItems.size() == 0) {
            return
        }
        val playlist = ArrayList<Song>()
        mItems.forEach {
            playlist.addAll(homefy.getLibrary().getAlbumSongs(it))
        }
        homefy.getPlayer().play(playlist.first(), playlist)
    }

    private class AlbumSorter(adapter: AlbumAdapter) : SortedListAdapterCallback<String>(adapter) {

        override fun areItemsTheSame(item1: String?, item2: String?): Boolean {
            return item1 == item2
        }

        override fun compare(o1: String?, o2: String?): Int {
            if (o1 == null || o2 == null) {
                return 1
            }
            return o1.compareTo(o2)
        }

        override fun areContentsTheSame(oldItem: String?, newItem: String?): Boolean {
            return false
        }

    }

    class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val albumTitle: TextView = view.albumTitle
        val artistNames: TextView = view.artistNames
        val albumBase: ConstraintLayout = view.albumViewBase
    }
}



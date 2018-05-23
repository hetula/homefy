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
import kotlinx.android.synthetic.main.list_item_artist.view.*
import xyz.hetula.homefy.R
import xyz.hetula.homefy.forEach
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.HomefyService

class ArtistAdapter(private val originalArtists: List<String>,
                    private val homefy: HomefyService,
                    private val onArtistClick: (String) -> Unit) :
        RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>(), SearchableAdapter<String> {

    override val mItems = SortedList<String>(String::class.java, ArtistSorter(this))
    override var mLastSearch: String = ""
    override var mCurrentSearchTask: SearchTask<String>? = null

    init {
        mItems.addAll(originalArtists)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ArtistViewHolder(inflater.inflate(R.layout.list_item_artist, parent, false))
    }

    override fun getOriginalItems() = originalArtists

    override fun getItemCount() = mItems.size()

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = mItems[position]
        holder.artistTitle.text = artist
        holder.artistViewBase.setOnClickListener {
            onArtistClick(artist)
        }
        val songs = homefy.getLibrary().getArtistSongs(artist)
        val count = songs.map { it.album }.distinct().count()
        val context = holder.itemView.context
        val albumCountText = context.resources.getQuantityString(R.plurals.album_count, count, count)
        val songCountText = context.resources.getQuantityString(R.plurals.song_count, songs.size, songs.size)
        holder.albumCount.text = context.getString(R.string.library_album_and_song_count, albumCountText, songCountText)
    }

    override fun searchFilter(item: String, search: String) = !item.contains(search, true)

    override fun playAll() {
        if(mItems.size() == 0) {
            return
        }
        val playlist = ArrayList<Song>()
        mItems.forEach {
            playlist.addAll(homefy.getLibrary().getArtistSongs(it))
        }
        homefy.getPlayer().play(playlist.first(), playlist)
    }

    private class ArtistSorter(adapter: ArtistAdapter) : SortedListAdapterCallback<String>(adapter) {
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
            return oldItem.equals(newItem)
        }
    }

    class ArtistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val artistViewBase: ConstraintLayout = view.artistViewBase
        val artistTitle: TextView = view.artistTitle
        val albumCount: TextView = view.albumCount
    }
}

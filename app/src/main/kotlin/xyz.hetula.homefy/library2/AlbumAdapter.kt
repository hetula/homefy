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
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import kotlinx.android.synthetic.main.list_item_album.view.*
import xyz.hetula.homefy.R

class AlbumAdapter(albums: List<String>, private val onAlbumClick: (String) -> Unit) :
        RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {
    private val mAlbums = SortedList<String>(String::class.java, AlbumSorter(this))

    init {
        mAlbums.addAll(albums)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AlbumViewHolder(inflater.inflate(R.layout.list_item_album, parent, false))
    }

    override fun getItemCount() = mAlbums.size()

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = mAlbums[position]
        holder.albumTitle.text = album
        holder.albumBase.setOnClickListener {
            onAlbumClick(album)
        }
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
            return oldItem.equals(newItem)
        }

    }

    class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val albumArtView: ImageView = view.albumArtView
        val albumTitle: TextView = view.albumTitle
        val albumBase: ConstraintLayout = view.albumViewBase
    }
}



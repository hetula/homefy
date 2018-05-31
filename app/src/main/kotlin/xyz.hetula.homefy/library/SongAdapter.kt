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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.emoji.widget.EmojiAppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import kotlinx.android.synthetic.main.list_item_song.view.*
import xyz.hetula.homefy.R
import xyz.hetula.homefy.forEach
import xyz.hetula.homefy.player.HomefyPlayer
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.playlist.FavoriteChangeListener
import xyz.hetula.homefy.playlist.HomefyPlaylist

class SongAdapter(private val originalSongs: List<Song>,
                  private val mPlayer: HomefyPlayer,
                  private val mPlaylists: HomefyPlaylist) :
        RecyclerView.Adapter<SongAdapter.SongViewHolder>(), SearchableAdapter<Song>, FavoriteChangeListener {

    override val mItems: SortedList<Song> = SortedList(Song::class.java, SongSorter(this))
    override var mLastSearch: String = ""
    override var mCurrentSearchTask: SearchTask<Song>? = null

    init {
        mItems.addAll(originalSongs)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SongViewHolder(inflater.inflate(R.layout.list_item_song, parent, false))
    }

    override fun getOriginalItems() = originalSongs

    override fun getItemCount() = mItems.size()

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val context = holder.itemView.context
        val song = mItems[position]
        holder.songTrackAndTitle.text = if (song.track < 1) {
            song.title
        } else {
            context.getString(R.string.library_song_track_title, song.track, song.title)
        }
        holder.songArtistAndAlbum.text = context.getString(R.string.library_song_artist_album, song.artist, song.album)

        holder.songFavorite.setOnCheckedChangeListener { button, isFavorite ->
            if (button.isPressed) {
                setFavorite(button.context, song, isFavorite)
            }
        }
        holder.songFavorite.isChecked = mPlaylists.isFavorite(song)

        holder.songBase.setOnClickListener {
            playSongWithCurrentList(song)
        }
    }

    override fun onFavoriteChanged(song: Song) {
        val index = mItems.indexOf(song)
        if (index != -1) {
            mItems.updateItemAt(index, song)
        }
    }

    private fun setFavorite(context: Context, song: Song, isFav: Boolean) {
        if (isFav) {
            mPlaylists.favorites.add(context, mPlaylists, song)
        } else {
            mPlaylists.favorites.remove(context, mPlaylists, song)
        }
    }

    override fun playAll() {
        if (mItems.size() == 0) {
            return
        }
        val playlist = ArrayList<Song>()
        mItems.forEach { playlist.add(it) }
        mPlayer.play(playlist.first(), playlist)
    }

    private fun playSongWithCurrentList(song: Song) {
        val playContext = ArrayList<Song>()
        mItems.forEach { playContext.add(it) }
        mPlayer.play(song, playContext)
    }

    override fun searchFilter(item: Song, search: String) = !item.title.contains(search, true)

    class SongSorter(adapter: RecyclerView.Adapter<SongViewHolder>) : SortedListAdapterCallback<Song>(adapter) {
        override fun areItemsTheSame(s1: Song?, s2: Song?): Boolean {
            if (s1 == null || s2 == null) {
                return false
            }
            return s1.id == s2.id
        }

        override fun compare(s1: Song?, s2: Song?): Int {
            if (s1 == null || s2 == null) {
                return 1
            }
            return s1.compareTo(s2)
        }

        override fun areContentsTheSame(oldItem: Song?, newItem: Song?): Boolean {
            if (oldItem == null || newItem == null) {
                return false
            }
            return oldItem == newItem
        }

    }

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songFavorite: CheckBox = view.songFavorite
        val songTrackAndTitle: EmojiAppCompatTextView = view.songTrackAndTitle
        val songArtistAndAlbum: EmojiAppCompatTextView = view.songArtistAndAlbum
        val songBase: ConstraintLayout = view.songBase
    }
}


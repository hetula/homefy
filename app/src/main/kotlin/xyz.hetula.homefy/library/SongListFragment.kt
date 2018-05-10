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

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_song_list.view.*
import xyz.hetula.homefy.HomefyFragment
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.Song

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class SongListFragment : HomefyFragment() {
    private var mParentTitle: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_song_list, container, false)
        root.recyclerView!!.setHasFixedSize(true)
        root.recyclerView.layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false)

        val type = arguments!!.getInt(LIST_TYPE_KEY)
        val name = arguments!!.getString(LIST_NAME_KEY, "Invalid Name")

        val adapter: RecyclerView.Adapter<*>
        when (type) {
            ALL_MUSIC -> {
                adapter = SongAdapter(homefy().getLibrary().songs,
                        homefy().getPlayer(), homefy().getPlaylists())
                (activity as AppCompatActivity).supportActionBar?.title =
                        getString(R.string.library_music)
            }
            ARTISTS -> {
                adapter = SongListAdapter(homefy().getLibrary().artists,
                        homefy().getPlayer(),
                        { artist -> homefy().getLibrary().getArtistSongs(artist) },
                        this::onArtistClick)
                (activity as AppCompatActivity).supportActionBar?.title =
                        getString(R.string.library_artists)
            }
            ALBUMS -> {
                adapter = SongListAdapter(homefy().getLibrary().albums,
                        homefy().getPlayer(),
                        { album -> homefy().getLibrary().getAlbumSongs(album) },
                        this::onAlbumClick)
                (activity as AppCompatActivity).supportActionBar?.title =
                        getString(R.string.library_albums)
            }
            ARTIST_MUSIC -> {
                adapter = SongAdapter(homefy().getLibrary().getArtistSongs(name),
                        homefy().getPlayer(), homefy().getPlaylists())
                mParentTitle = getString(R.string.library_artists)
                (activity as AppCompatActivity).supportActionBar?.title = name
            }
            ALBUM_MUSIC -> {
                adapter = SongAdapter(homefy().getLibrary().getAlbumSongs(name),
                        homefy().getPlayer(), homefy().getPlaylists())
                mParentTitle = getString(R.string.library_albums)
                (activity as AppCompatActivity).supportActionBar?.title = name
            }
            FAVORITES -> {
                adapter = SongAdapter(homefy().getPlaylists().favorites.songs,
                        homefy().getPlayer(), homefy().getPlaylists(), this::onFavClick)
                (activity as AppCompatActivity).supportActionBar?.title =
                        getString(R.string.library_favs)
            }
            PLAYLIST -> {
                val playlist = homefy().getPlaylists()[name] ?:
                        throw IllegalArgumentException("Calling SongListFragment with invalid playlist id: $name")

                adapter = SongAdapter(playlist.songs, homefy().getPlayer(),
                        homefy().getPlaylists(), playlist = playlist)
                (activity as AppCompatActivity).supportActionBar?.title = playlist.name
                mParentTitle = getString(R.string.library_playlists)
            }
            else -> {
                Log.e(TAG, "Invalid TYPE: $type")
                throw IllegalArgumentException("Calling SongListFragment with invalid type: $type")
            }
        }
        root.recyclerView.adapter = adapter
        return root
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mParentTitle.isBlank()) {
            (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        } else {
            (activity as AppCompatActivity).supportActionBar?.title = mParentTitle
        }
    }

    private fun onFavClick(adapter: SongAdapter, song: Song) {
        if (homefy().getPlaylists().isFavorite(song)) {
            adapter.add(song)
        } else {
            adapter.remove(song)
        }
    }

    private fun onArtistClick(artist: String) {
        val args = Bundle()
        args.putInt(LIST_TYPE_KEY, ARTIST_MUSIC)
        args.putString(LIST_NAME_KEY, artist)
        createSongListFragment(args)
    }

    private fun onAlbumClick(album: String) {
        val args = Bundle()
        args.putInt(LIST_TYPE_KEY, ALBUM_MUSIC)
        args.putString(LIST_NAME_KEY, album)
        createSongListFragment(args)
    }

    private fun createSongListFragment(args: Bundle) {
        val fragment = SongListFragment()
        fragment.arguments = args
        fragmentManager!!
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .replace(R.id.container, fragment)
                .commit()
    }

    companion object {
        const val TAG = "SongListFragment"
        const val LIST_TYPE_KEY = "SongListFragment_LIST_TYPE_KEY"
        const val LIST_NAME_KEY = "SongListFragment_LIST_NAME_KEY"

        const val ALL_MUSIC = 1
        const val ARTISTS = 2
        const val ALBUMS = 3
        const val ARTIST_MUSIC = 4
        const val ALBUM_MUSIC = 5
        const val FAVORITES = 6
        const val PLAYLIST = 7
    }
}

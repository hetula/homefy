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

import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                        getString(R.string.nav_music)
            }
            ARTISTS -> {
                adapter = SongListAdapter(homefy().getLibrary().artists,
                        homefy().getPlayer(),
                        { artist -> homefy().getLibrary().getArtistSongs(artist) },
                        this::onArtistClick)
                (activity as AppCompatActivity).supportActionBar?.title =
                        getString(R.string.nav_artists)
            }
            ALBUMS -> {
                adapter = SongListAdapter(homefy().getLibrary().albums,
                        homefy().getPlayer(),
                        { album -> homefy().getLibrary().getAlbumSongs(album) },
                        this::onAlbumClick)
                (activity as AppCompatActivity).supportActionBar?.title =
                        getString(R.string.nav_albums)
            }
            ARTIST_MUSIC -> {
                adapter = SongAdapter(homefy().getLibrary().getArtistSongs(name),
                        homefy().getPlayer(), homefy().getPlaylists())
                mParentTitle = getString(R.string.nav_artists)
                (activity as AppCompatActivity).supportActionBar?.title = name
            }
            ALBUM_MUSIC -> {
                adapter = SongAdapter(homefy().getLibrary().getAlbumSongs(name),
                        homefy().getPlayer(), homefy().getPlaylists())
                mParentTitle = getString(R.string.nav_albums)
                (activity as AppCompatActivity).supportActionBar?.title = name
            }
            FAVORITES -> {
                adapter = SongAdapter(homefy().getPlaylists().favorites.songs,
                        homefy().getPlayer(), homefy().getPlaylists(), this::onFavClick)
                (activity as AppCompatActivity).supportActionBar?.title =
                        getString(R.string.nav_favs)
            }
            PLAYLIST -> {
                val playlist = homefy().getPlaylists()[name] ?:
                        throw IllegalArgumentException("Calling SongListFragment with invalid playlist id: $name")

                adapter = SongAdapter(playlist.songs, homefy().getPlayer(),
                        homefy().getPlaylists(), playlist = playlist)
                (activity as AppCompatActivity).supportActionBar?.title = playlist.name
                mParentTitle = getString(R.string.nav_playlists)
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

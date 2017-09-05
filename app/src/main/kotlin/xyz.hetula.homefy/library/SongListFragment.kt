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
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_song_list.view.*
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.Homefy

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class SongListFragment : Fragment() {
    private var mParentTitle: String = ""

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_song_list, container, false)
        root.recyclerView!!.setHasFixedSize(true)
        root.recyclerView.layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false)

        val type = arguments.getInt(LIST_TYPE_KEY)
        val name = arguments.getString(LIST_NAME_KEY, "Invalid Name")

        val adapter: RecyclerView.Adapter<*>
        when (type) {
            ALL_MUSIC -> {
                adapter = SongAdapter(Homefy.library().songs)
                (activity as AppCompatActivity).supportActionBar?.title =
                        context.getString(R.string.nav_music)
            }
            ARTISTS -> {
                adapter = SongListAdapter(Homefy.library().artists,
                        { artist -> Homefy.library().getArtistSongs(artist) },
                        this::onArtistClick)
                (activity as AppCompatActivity).supportActionBar?.title =
                        context.getString(R.string.nav_artists)
            }
            ALBUMS -> {
                adapter = SongListAdapter(Homefy.library().albums,
                        { album -> Homefy.library().getAlbumSongs(album) },
                        this::onAlbumClick)
                (activity as AppCompatActivity).supportActionBar?.title =
                        context.getString(R.string.nav_albums)
            }
            ARTIST_MUSIC -> {
                adapter = SongAdapter(Homefy.library().getArtistSongs(name))
                mParentTitle = context.getString(R.string.nav_artists)
                (activity as AppCompatActivity).supportActionBar?.title = name
            }
            ALBUM_MUSIC -> {
                adapter = SongAdapter(Homefy.library().getAlbumSongs(name))
                mParentTitle = context.getString(R.string.nav_albums)
                (activity as AppCompatActivity).supportActionBar?.title = name
            }
            FAVORITES -> {
                adapter = SongAdapter(Homefy.playlist().favorites.songs, this::onFavClick)
                (activity as AppCompatActivity).supportActionBar?.title =
                        context.getString(R.string.nav_favs)
            }
            PLAYLIST -> {
                val playlist = Homefy.playlist()[name] ?:
                        throw IllegalArgumentException("Calling SongListFragment with invalid playlist id: $name")

                adapter = SongAdapter(playlist.songs)
                (activity as AppCompatActivity).supportActionBar?.title = playlist.name
                mParentTitle = context.getString(R.string.nav_playlists)
            }
            else -> {
                Log.e(TAG, "Invalid TYPE: " + type)
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
            (activity as AppCompatActivity).supportActionBar?.title = context.getString(R.string.app_name)
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        } else {
            (activity as AppCompatActivity).supportActionBar?.title = mParentTitle
        }
    }

    private fun onFavClick(adapter: SongAdapter, song: Song) {
        if (Homefy.playlist().isFavorite(song)) adapter.add(song)
        else adapter.remove(song)
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
        fragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .hide(this)
                .addToBackStack(null)
                .add(R.id.container, fragment)
                .show(fragment)
                .commit()
    }

    companion object {
        val TAG = "SongListFragment"
        val LIST_TYPE_KEY = "SongListFragment_LIST_TYPE_KEY"
        val LIST_NAME_KEY = "SongListFragment_LIST_NAME_KEY"

        val ALL_MUSIC = 1
        val ARTISTS = 2
        val ALBUMS = 3
        val ARTIST_MUSIC = 4
        val ALBUM_MUSIC = 5
        val FAVORITES = 6
        val PLAYLIST = 7
    }
}

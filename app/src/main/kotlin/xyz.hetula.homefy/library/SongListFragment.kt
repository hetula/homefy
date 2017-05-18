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
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import xyz.hetula.homefy.R
import xyz.hetula.homefy.service.Homefy

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class SongListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_song_list, container, false)
        val recyclerView = root.findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false)

        val args = arguments
        val type = args.getInt(LIST_TYPE_KEY)
        val name = args.getString(LIST_NAME_KEY, "ERRORROROR")

        val adapter: RecyclerView.Adapter<*>?
        when (type) {
            ALL_MUSIC -> adapter = SongAdapter(Homefy.library().songs)
            ARTISTS -> adapter = SongListAdapter(Homefy.library().artists,
                    { artist -> Homefy.library().getArtistSongs(artist).size },
                    this::onArtistClick)
            ALBUMS -> adapter = SongListAdapter(Homefy.library().albums,
                    { album -> Homefy.library().getAlbumSongs(album).size },
                    this::onAlbumClick)
            ARTIST_MUSIC -> adapter = SongAdapter(Homefy.library().getArtistSongs(name))
            ALBUM_MUSIC -> adapter = SongAdapter(Homefy.library().getAlbumSongs(name))
            else -> {
                adapter = null
                Log.e("SongListFragment", "Invalid TYPE: " + type)
            }
        }
        if (adapter != null) {
            recyclerView.adapter = adapter
        }
        return root
    }

    private fun onArtistClick(artist: String) {
        val args = Bundle()
        args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ARTIST_MUSIC)
        args.putString(SongListFragment.LIST_NAME_KEY, artist)
        createSongListFragment(args)
    }

    private fun onAlbumClick(album: String) {
        val args = Bundle()
        args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ALBUM_MUSIC)
        args.putString(SongListFragment.LIST_NAME_KEY, album)
        createSongListFragment(args)
    }

    private fun createSongListFragment(args: Bundle) {
        val fragment = SongListFragment()
        fragment.arguments = args
        fragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .add(R.id.container, fragment)
                .show(fragment)
                .commit()
    }

    companion object {
        val LIST_TYPE_KEY = "SongListFragment_LIST_TYPE_KEY"
        val LIST_NAME_KEY = "SongListFragment_LIST_NAME_KEY"

        val ALL_MUSIC = 1
        val ARTISTS = 2
        val ALBUMS = 3
        val ARTIST_MUSIC = 4
        val ALBUM_MUSIC = 5
    }
}

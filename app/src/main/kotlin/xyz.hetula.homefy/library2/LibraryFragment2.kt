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

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_library2.view.*
import xyz.hetula.homefy.HomefyFragment
import xyz.hetula.homefy.R

class LibraryFragment2 : HomefyFragment() {
    private var mCurrentTab = NavigationTab.NONE
    private var mLastSelectedIndex = R.id.navSongs
    private lateinit var mNavBar: BottomNavigationView
    private lateinit var mLibraryList: RecyclerView
    private lateinit var mNowPlayingView: FrameLayout
    private lateinit var mTopSearch: FrameLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val main = inflater.inflate(R.layout.fragment_library2, container, false) as LinearLayout
        mNavBar = main.navBar
        main.navBar.setOnNavigationItemSelectedListener { navBarItemClick(it) }
        mLibraryList = main.libraryList
        mNowPlayingView = main.nowPlayingView
        mTopSearch = main.topSearch

        mCurrentTab = NavigationTab.NONE
        selectTabItem(mLastSelectedIndex)
        return main
    }

    private fun navBarItemClick(item: MenuItem): Boolean {
        mLastSelectedIndex = item.itemId
        return selectTabItem(item.itemId)
    }

    private fun selectTabItem(itemId: Int): Boolean {
        return when (itemId) {
            R.id.navSongs -> {
                selectSongTab()
                true
            }
            R.id.navAlbums -> {
                selectAlbumTab()
                true
            }
            R.id.navArtists -> {
                selectArtistTab()
                true
            }
            R.id.navPlaylists -> {
                selectPlaylistTab()
                true
            }
            R.id.navNowPlaying -> {
                selectNowPlayingTab()
                true
            }
            else -> {
                Log.w(TAG, "Invalid tab: $itemId")
                false
            }
        }
    }

    private fun selectSongTab() = selectTabIfNotSelected(NavigationTab.SONG) {
        mNowPlayingView.visibility = View.GONE
        mTopSearch.visibility = View.VISIBLE
        mLibraryList.visibility = View.VISIBLE
        mLibraryList.adapter = SongAdapter2(homefy().getLibrary().songs,
                homefy().getPlayer(),
                homefy().getPlaylists())
    }

    private fun selectAlbumTab() = selectTabIfNotSelected(NavigationTab.ALBUM) {
        mNowPlayingView.visibility = View.GONE
        mTopSearch.visibility = View.VISIBLE
        mLibraryList.visibility = View.VISIBLE
        mLibraryList.adapter = AlbumAdapter(homefy().getLibrary().albums, homefy(), this::showAlbumDialog)
    }

    private fun selectArtistTab() = selectTabIfNotSelected(NavigationTab.ARTIST) {
        mNowPlayingView.visibility = View.GONE
        mTopSearch.visibility = View.VISIBLE
        mLibraryList.visibility = View.VISIBLE
        mLibraryList.adapter = ArtistAdapter(homefy().getLibrary().artists, homefy(), this::showArtistDialog)
    }

    private fun selectPlaylistTab() = selectTabIfNotSelected(NavigationTab.PLAYLIST) {
        mNowPlayingView.visibility = View.GONE
        mTopSearch.visibility = View.VISIBLE
        mLibraryList.visibility = View.VISIBLE
        mLibraryList.adapter = null
    }

    private fun selectNowPlayingTab() = selectTabIfNotSelected(NavigationTab.NOW_PLAYING) {
        mNowPlayingView.visibility = View.VISIBLE
        mTopSearch.visibility = View.GONE
        mLibraryList.visibility = View.GONE
        mLibraryList.adapter = null

    }

    private fun selectTabIfNotSelected(tab: NavigationTab, function: () -> Unit) {
        if (mCurrentTab != tab) {
            mLibraryList.layoutManager = LinearLayoutManager(context)
            function()
            mCurrentTab = tab
        } else {
            Log.i(TAG, "Navigating to already selected tab: $tab")
        }
    }

    private fun showArtistDialog(artist: String) {
        val context = context ?: return
        val songs = homefy().getLibrary().getArtistSongs(artist)
        Log.d(TAG, "$songs")
        val albumCount = songs.map { it.album }.distinct().count()
        val subtitle = context.resources.getQuantityString(R.plurals.album_count, albumCount, albumCount)
        val dilFrag = SongsFragment()
        dilFrag.setup(homefy(), artist, subtitle, songs)
        fragmentManager!!.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, dilFrag)
                .addToBackStack("SongsView_$artist")
                .commit()
    }

    private fun showAlbumDialog(album: String) {
        val songs = homefy().getLibrary().getAlbumSongs(album)
        Log.d(TAG, "$songs")
        val subtitle = songs.map { it.artist }.distinct().reduce { allArtists, artist ->
            "$allArtists, $artist"
        }
        val dilFrag = SongsFragment()
        dilFrag.setup(homefy(), album, subtitle, songs)
        fragmentManager!!.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, dilFrag)
                .addToBackStack("SongsView_$album")
                .commit()
    }

    private enum class NavigationTab {
        SONG,
        ALBUM,
        ARTIST,
        PLAYLIST,
        NOW_PLAYING,
        NONE
    }

    companion object {
        private const val TAG = "LibraryFragment"
    }
}

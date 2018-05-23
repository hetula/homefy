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

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_library2.view.*
import xyz.hetula.homefy.HomefyFragment
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.PlayerView
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.playlist.Playlist

class LibraryFragment2 : HomefyFragment() {
    private var mCurrentTab = NavigationTab.NONE
    private var mLastSelectedTab = R.id.navSongs
    private lateinit var mHandler: Handler
    private lateinit var mMain: View
    private lateinit var mInputManager: InputMethodManager
    private lateinit var mNavBar: BottomNavigationView
    private lateinit var mLibraryList: RecyclerView
    private lateinit var mNowPlayingView: PlayerView
    private lateinit var mTopSearch: FrameLayout
    private lateinit var mSearchField: EditText
    private lateinit var mLibraryHolder: FrameLayout
    private lateinit var mPlayAllFloat: FloatingActionButton


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mHandler = Handler()
        mInputManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val selectTab = selectTab()
        if (selectTab != 0) {
            mLastSelectedTab = selectTab
        }

        val main = inflater.inflate(R.layout.fragment_library2, container, false) as LinearLayout
        mMain = main
        mNavBar = main.navBar
        if (selectTab != 0) {
            mNavBar.selectedItemId = selectTab
        }
        main.navBar.setOnNavigationItemSelectedListener(::navBarItemClick)
        mLibraryList = main.libraryList
        mNowPlayingView = main.nowPlayingView
        mNowPlayingView.setHomefy(homefy())
        mTopSearch = main.topSearch
        mSearchField = main.searchField
        mLibraryHolder = main.libraryHolder
        mPlayAllFloat = main.playAllFloat

        mSearchField.addTextChangedListener(SearchTextListener(::searchWith))
        mSearchField.setOnEditorActionListener { view, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                    keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                view.clearFocus()
                mMain.focusEater.requestFocus()
                hideSoftKeyboard()
                true
            } else {
                false
            }
        }

        mPlayAllFloat.setOnClickListener {
            playAll()
        }

        mCurrentTab = NavigationTab.NONE
        selectTabItem(mLastSelectedTab)
        return main
    }


    private fun hideSoftKeyboard() {
        mHandler.post {
            mInputManager.hideSoftInputFromWindow(mMain.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    private fun searchWith(search: String) {
        if (mCurrentTab.noSearch()) {
            return
        }
        val adapter = mLibraryList.adapter ?: return
        if (adapter is SearchableAdapter<*>) {
            adapter.searchWith(search)
        }
    }

    private fun navBarItemClick(item: MenuItem): Boolean {
        mLastSelectedTab = item.itemId
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
        mLibraryList.adapter = SongAdapter2(homefy().getLibrary().songs,
                homefy().getPlayer(),
                homefy().getPlaylists())
        mNowPlayingView.visibility = View.GONE
        mLibraryHolder.visibility = View.VISIBLE
    }

    private fun selectAlbumTab() = selectTabIfNotSelected(NavigationTab.ALBUM) {
        mLibraryList.adapter = AlbumAdapter(homefy().getLibrary().albums, homefy(), this::showAlbumDialog)
        mNowPlayingView.visibility = View.GONE
        mLibraryHolder.visibility = View.VISIBLE
    }

    private fun selectArtistTab() = selectTabIfNotSelected(NavigationTab.ARTIST) {
        mLibraryList.adapter = ArtistAdapter(homefy().getLibrary().artists, homefy(), this::showArtistDialog)
        mNowPlayingView.visibility = View.GONE
        mLibraryHolder.visibility = View.VISIBLE
    }

    private fun selectPlaylistTab() = selectTabIfNotSelected(NavigationTab.PLAYLIST) {
        val playlists = ArrayList<Playlist>()
        playlists.add(homefy().getPlaylists().favorites)
        playlists.addAll(homefy().getPlaylists().getAllPlaylists())
        mLibraryList.adapter = PlaylistAdapter(playlists, this::showPlaylistContent)
        mNowPlayingView.visibility = View.GONE
        mLibraryHolder.visibility = View.VISIBLE
    }

    private fun selectNowPlayingTab() = selectTabIfNotSelected(NavigationTab.NOW_PLAYING) {
        mNowPlayingView.visibility = View.VISIBLE
        mLibraryHolder.visibility = View.GONE
        mLibraryList.adapter = null

    }

    private fun selectTabIfNotSelected(tab: NavigationTab, function: () -> Unit) {
        if (mCurrentTab != tab) {
            hideSoftKeyboard()
            mLibraryList.layoutManager = LinearLayoutManager(context)
            mLibraryList.adapter = null
            mSearchField.text.clear()
            mTopSearch.visibility = if (tab.noSearch()) {
                mPlayAllFloat.hide()
                View.GONE
            } else {
                mPlayAllFloat.show()
                View.VISIBLE
            }
            function()
            if (tab == NavigationTab.NOW_PLAYING) {
                mNowPlayingView.show()
            } else if (mCurrentTab == NavigationTab.NOW_PLAYING) {
                mNowPlayingView.hide()
            }
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
        showSongFragment(artist, subtitle, songs)
    }

    private fun showAlbumDialog(album: String) {
        val songs = homefy().getLibrary().getAlbumSongs(album)
        val subtitle = songs.map { it.artist }.distinct().reduce { allArtists, artist ->
            "$allArtists, $artist"
        }
        showSongFragment(album, subtitle, songs)
    }

    private fun showPlaylistContent(playlist: Playlist) {
        showSongFragment(playlist.name, "", playlist.songs)
    }

    private fun showSongFragment(title: String, subtitle: String, songs: List<Song>) {
        val songsFragment = SongsFragment()
        // TODO: This should use proper setting methods, not garbage like this as it can be destroyed.
        songsFragment.setup(homefy(), title, subtitle, songs)
        fragmentManager!!.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, songsFragment)
                .addToBackStack("SongsView_$title")
                .commit()
    }

    private fun playAll() {
        if (mCurrentTab.noSearch()) {
            return
        }
        val adapter = mLibraryList.adapter ?: return
        if (adapter is SearchableAdapter<*>) {
            adapter.playAll()
        }
    }

    private class SearchTextListener(private val onTextChanged: (String) -> Unit) : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            onTextChanged(p0.toString())
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Not interested
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Not interested
        }
    }


    private enum class NavigationTab(private val searchable: Boolean = false) {
        SONG(true),
        ALBUM(true),
        ARTIST(true),
        PLAYLIST,
        NOW_PLAYING,
        NONE;

        fun noSearch(): Boolean = !searchable
    }

    companion object {
        private const val TAG = "LibraryFragment"
    }
}

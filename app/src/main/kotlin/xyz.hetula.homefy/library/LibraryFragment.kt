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
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_library.view.*
import xyz.hetula.homefy.HomefyActivity
import xyz.hetula.homefy.HomefyFragment
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.PlayerView
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.playlist.FavoriteChangeListener
import xyz.hetula.homefy.playlist.FavoriteChangeReceiver
import xyz.hetula.homefy.playlist.Playlist
import java.util.*

class LibraryFragment : HomefyFragment() {
    private var mCurrentTab = NavigationTab.NONE
    private var mLastSelectedTab = R.id.navSongs
    private lateinit var mHandler: Handler
    private lateinit var mMain: View
    private lateinit var mInputManager: InputMethodManager
    private lateinit var mNavBar: BottomNavigationView
    private lateinit var mLibraryList: RecyclerView
    private lateinit var mNowPlayingView: PlayerView
    private lateinit var mTopSearch: FrameLayout
    private lateinit var mTopTitle: TextView
    private lateinit var mSearchField: EditText
    private lateinit var mLibraryHolder: FrameLayout
    private lateinit var mPlayAllFloat: FloatingActionButton
    private lateinit var mFavoriteChangeReceiver: FavoriteChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mHandler = Handler()
        mInputManager = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mFavoriteChangeReceiver = FavoriteChangeReceiver(homefy().getLibrary(), ::updateFavoriteListeners)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mMain = inflater.inflate(R.layout.fragment_library, container, false) as LinearLayout
        mNavBar = mMain.navBar
        mLibraryList = mMain.libraryList
        mNowPlayingView = mMain.nowPlayingView
        mTopSearch = mMain.topSearch
        mTopTitle = mMain.topTitle
        mSearchField = mMain.searchField
        mLibraryHolder = mMain.libraryHolder
        mPlayAllFloat = mMain.playAllFloat

        mNowPlayingView.setHomefy(homefy())
        mNavBar.setOnNavigationItemSelectedListener(::navBarItemClick)
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
            onFloatAction()
        }
        mNowPlayingView.setOnDownloadClick {
            (activity as HomefyActivity).download(homefy().getPlayer().nowPlaying())
        }

        mCurrentTab = NavigationTab.NONE // Clean for fresh setup

        if (LibraryFragment.openPlaying) {
            getAndClearNewSetupTab() // Clean
            LibraryFragment.openPlaying = false
            selectPlayingTab()
        } else {
            val selectTab = getAndClearNewSetupTab()
            if (selectTab != 0) {
                Log.d(TAG, "Overriding with selected tab!")
                mLastSelectedTab = selectTab
                mNavBar.selectedItemId = selectTab
            }
            selectTabItem(mLastSelectedTab)
        }
        mFavoriteChangeReceiver.register(context!!)
        return mMain
    }

    override fun onDestroyView() {
        mFavoriteChangeReceiver.unregister(context!!)
        super.onDestroyView()
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
        if (adapter is BaseAdapter<*>) {
            adapter.searchWith(search)
        }
    }

    private fun navBarItemClick(item: MenuItem): Boolean {
        mLastSelectedTab = item.itemId
        return selectTabItem(item.itemId)
    }

    private fun selectTabItem(itemId: Int): Boolean {
        Log.d(TAG, "Selecting Tab item: $itemId")
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
        mLibraryList.adapter = initAdapter(SongAdapter(homefy().getLibrary().songs,
                homefy().getPlayer(),
                homefy().getPlaylists()))
        mNowPlayingView.visibility = View.GONE
        mLibraryHolder.visibility = View.VISIBLE
    }

    private fun selectAlbumTab() = selectTabIfNotSelected(NavigationTab.ALBUM) {
        mLibraryList.adapter = initAdapter(AlbumAdapter(homefy().getLibrary().albums, homefy(), this::showAlbumDialog))
        mNowPlayingView.visibility = View.GONE
        mLibraryHolder.visibility = View.VISIBLE
    }

    private fun selectArtistTab() = selectTabIfNotSelected(NavigationTab.ARTIST) {
        mLibraryList.adapter = initAdapter(ArtistAdapter(homefy().getLibrary().artists, homefy(), this::showArtistDialog))
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

    private fun selectTabIfNotSelected(tab: NavigationTab, onSelect: () -> Unit) {
        if (mCurrentTab != tab) {
            hideSoftKeyboard()
            mLibraryList.layoutManager = LinearLayoutManager(context)
            mLibraryList.adapter = null
            mSearchField.text.clear()
            mSearchField.visibility = if (tab.noSearch()) {
                View.GONE
            } else {
                View.VISIBLE
            }
            mTopTitle.visibility = if (tab != NavigationTab.PLAYLIST) {
                View.GONE
            } else {
                View.VISIBLE
            }
            mTopSearch.visibility = if (tab == NavigationTab.NOW_PLAYING) {
                View.GONE
            } else {
                View.VISIBLE
            }


            mPlayAllFloat.hide()
            if (tab.hasActionButton()) {
                mPlayAllFloat.setImageResource(tab.fabIcon)
                mHandler.post(mPlayAllFloat::show)
            }
            onSelect()
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
        val args = Bundle()
        args.putString(SongsFragment.ARGUMENT_TITLE, title)
        args.putString(SongsFragment.ARGUMENT_SUBTITLE, subtitle)
        args.putStringArray(SongsFragment.ARGUMENT_SONGS, songs.map { it.id }.toTypedArray())
        songsFragment.arguments = args
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
        if (adapter is BaseAdapter<*>) {
            adapter.playAll()
        }
    }

    private fun addPlaylist(playlist: Playlist) {
        val adapter = mLibraryList.adapter
        if (adapter is PlaylistAdapter) {
            adapter.addPlaylist(playlist)
        }
    }

    private fun newPlaylist() {
        val ask = AlertDialog.Builder(activity!!, R.style.AddPlaylistDialog)
        ask.setTitle(R.string.playlist_dialog_create_title)
        val txtName = EditText(context)
        txtName.inputType = InputType.TYPE_CLASS_TEXT
        ask.setView(txtName)
        ask.setPositiveButton(R.string.playlist_dialog_create) { d, _ ->
            val name = txtName.text.toString()
            if (name.isBlank()) {
                d.cancel()
            } else {
                addPlaylist(homefy().getPlaylists().createPlaylist(name))
            }
            mHandler.postDelayed(this::hideSoftKeyboard, 50)
        }
        ask.setNegativeButton(android.R.string.cancel) { d, _ ->
            mHandler.postDelayed(this::hideSoftKeyboard, 50)
            d.cancel()
        }
        ask.create().show()
    }

    private fun updateFavoriteListeners(song: Song) {
        val adapter = mLibraryList.adapter
        if (adapter is FavoriteChangeListener) {
            adapter.onFavoriteChanged(song)
        }
        if (mCurrentTab == NavigationTab.NOW_PLAYING) {
            mNowPlayingView.onFavoriteChanged(song)
        }
    }

    private fun onFloatAction() {
        val tab = mCurrentTab
        when (tab) {
            NavigationTab.SONG -> playAll()
            NavigationTab.ALBUM -> playAll()
            NavigationTab.ARTIST -> playAll()
            NavigationTab.PLAYLIST -> newPlaylist()
            else -> {
            }
        }
    }

    private fun <T : BaseAdapter<*>> initAdapter(adapter: T): T {
        adapter.onSongPlay = this::selectPlayingTab
        return adapter
    }

    private fun selectPlayingTab() {
        mHandler.post {
            mLastSelectedTab = R.id.navNowPlaying
            mNavBar.selectedItemId = mLastSelectedTab
            selectTabItem(mLastSelectedTab)
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


    private enum class NavigationTab(private val searchable: Boolean = false, @DrawableRes val fabIcon: Int = 0) {
        SONG(true, fabIcon = R.drawable.ic_shuffle_float),
        ALBUM(true, fabIcon = R.drawable.ic_shuffle_float),
        ARTIST(true, fabIcon = R.drawable.ic_shuffle_float),
        PLAYLIST(fabIcon = R.drawable.ic_add_to_playlist_24dp),
        NOW_PLAYING,
        NONE;

        fun noSearch(): Boolean = !searchable

        fun hasActionButton(): Boolean = fabIcon != 0
    }

    companion object {
        private const val TAG = "LibraryFragment"
        var openPlaying: Boolean = false
    }
}

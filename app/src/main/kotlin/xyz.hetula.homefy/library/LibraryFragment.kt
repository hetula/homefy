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

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_library.view.*
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.PlayerActivity
import xyz.hetula.homefy.playlist.PlaylistFragment

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class LibraryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val main = inflater.inflate(R.layout.fragment_library, container, false) as LinearLayout

        val clicks = View.OnClickListener { this.onLibraryClick(it) }
        main.library_music.setOnClickListener(clicks)
        main.library_artists.setOnClickListener(clicks)
        main.library_albums.setOnClickListener(clicks)
        main.library_search.setOnClickListener(clicks)
        main.library_favorites.setOnClickListener(clicks)
        main.library_playlists.setOnClickListener(clicks)
        main.homefy_header.setOnClickListener(clicks)
        return main
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.title = context!!.getString(R.string.app_name)
    }

    private fun onLibraryClick(v: View) {
        val args = Bundle()
        when (v.id) {
            R.id.library_music -> args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ALL_MUSIC)
            R.id.library_albums -> args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ALBUMS)
            R.id.library_artists -> args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ARTISTS)
            R.id.library_favorites -> args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.FAVORITES)
            else -> {
                onSpecialLibClick(v)
                return
            }
        }
        val fragment = SongListFragment()
        fragment.arguments = args
        openFragment(fragment)
    }

    private fun onSpecialLibClick(v: View) {
        when (v.id) {
            R.id.library_search -> openFragment(SongSearchFragment())
            R.id.library_playlists -> openFragment(PlaylistFragment())
            R.id.homefy_header -> openPlayer()
            else -> {
                Log.w("LibraryFragment", "Unhandled Click from $v")
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        fragmentManager!!
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .replace(R.id.container, fragment)
                .commit()
    }

    private fun openPlayer() {
        val intent = Intent(context, PlayerActivity::class.java)
        activity!!.startActivity(intent)
    }
}

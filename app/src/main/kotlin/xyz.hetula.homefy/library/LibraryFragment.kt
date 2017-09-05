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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val main = inflater!!.inflate(R.layout.fragment_library, container, false) as LinearLayout

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
                Log.w("LibraryFragment", "Unhandled Click from " + v)
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        fragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .hide(this)
                .addToBackStack(null)
                .add(R.id.container, fragment)
                .show(fragment)
                .commit()
    }

    private fun openPlayer() {
        val intent = Intent(context, PlayerActivity::class.java)
        activity.startActivity(intent)
    }
}

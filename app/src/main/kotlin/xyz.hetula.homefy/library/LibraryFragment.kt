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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import xyz.hetula.homefy.R

class LibraryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val main = inflater!!.inflate(R.layout.fragment_library, container, false) as FrameLayout
        val libraryMusic = main.findViewById(R.id.library_music)
        val libraryArtists = main.findViewById(R.id.library_artists)
        val libraryAlbums = main.findViewById(R.id.library_albums)

        val clicks = View.OnClickListener { this.onLibraryClick(it) }
        libraryMusic.setOnClickListener(clicks)
        libraryArtists.setOnClickListener(clicks)
        libraryAlbums.setOnClickListener(clicks)
        return main
    }

    private fun onLibraryClick(v: View) {
        val args = Bundle()
        when (v.id) {
            R.id.library_music -> args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ALL_MUSIC)
            R.id.library_albums -> args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ALBUMS)
            R.id.library_artists -> args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ARTISTS)
            else -> {
                Log.w("LibraryFragment", "Unhandled Click from " + v)
                return
            }
        }
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
}

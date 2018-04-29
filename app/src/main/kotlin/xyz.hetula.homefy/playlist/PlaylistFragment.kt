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
 */

package xyz.hetula.homefy.playlist

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import kotlinx.android.synthetic.main.fragment_playlist.view.*
import xyz.hetula.homefy.HomefyFragment
import xyz.hetula.homefy.R
import xyz.hetula.homefy.library.SongListFragment

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class PlaylistFragment : HomefyFragment() {
    private lateinit var mAdapter: PlaylistAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_playlist, container, false)
        root.btn_create_playlist.setOnClickListener { createPlaylist() }
        root.recyclerView.layoutManager = LinearLayoutManager(context)

        mAdapter = PlaylistAdapter {
            val args = Bundle()
            args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.PLAYLIST)
            args.putString(SongListFragment.LIST_NAME_KEY, it.id)

            val fragment = SongListFragment()
            fragment.arguments = args
            openFragment(fragment)
        }

        mAdapter.setPlaylists(homefy().getPlaylists().getAllPlaylists())
        root.recyclerView.adapter = mAdapter

        return root
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.nav_playlists)
        mAdapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun addPlaylist(playlist: Playlist) {
        mAdapter.addPlaylist(playlist)
    }

    private fun openFragment(fragment: Fragment) {
        fragmentManager!!
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .replace(R.id.container, fragment)
                .commit()
    }

    private fun createPlaylist() {
        val ask = AlertDialog.Builder(activity!!)
        ask.setTitle("New Playlist")
        val txtName = EditText(context)
        txtName.inputType = InputType.TYPE_CLASS_TEXT
        ask.setView(txtName)
        ask.setPositiveButton("Create", { d, _ ->
            val name = txtName.text.toString()
            if (name.isBlank()) {
                d.cancel()
            } else {
                addPlaylist(homefy().getPlaylists().createPlaylist(name))
            }
        })
        ask.setNegativeButton(android.R.string.cancel, { d, _ ->
            d.cancel()
        })
        ask.create().show()
    }
}

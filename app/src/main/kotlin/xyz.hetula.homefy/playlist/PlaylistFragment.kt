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

package xyz.hetula.homefy.playlist

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
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
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.library_playlists)
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

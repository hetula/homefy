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

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import kotlinx.android.synthetic.main.fragment_song_search.view.*
import xyz.hetula.homefy.HomefyFragment
import xyz.hetula.homefy.R

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class SongSearchFragment : HomefyFragment(), AdapterView.OnItemSelectedListener {
    private var mSearch: EditText? = null
    private var mAdapter: SongAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_song_search, container, false)

        val mRecycler = root.recyclerView
        mRecycler!!.setHasFixedSize(true)
        mRecycler.layoutManager = LinearLayoutManager(context)

        mAdapter = SongAdapter(homefy().getLibrary().songs, homefy().getPlayer(),
                homefy().getPlaylists())
        mRecycler.adapter = mAdapter

        val adapter = ArrayAdapter<SearchType>(context,
                android.R.layout.simple_spinner_item, SearchType.values())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        root.sp_search.adapter = adapter

        root.txt_search.addTextChangedListener(TypingListener(root.sp_search, this::doSearch))
        mSearch = root.txt_search

        return root
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.nav_search)
    }

    override fun onPause() {
        super.onPause()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun doSearch(search: Editable?, type: SearchType) {
        if (search == null) {
            mAdapter?.setSongs(homefy().getLibrary().songs)
            return
        }
        val text = search.toString()
        if (text.isBlank()) {
            mAdapter?.setSongs(homefy().getLibrary().songs)
            return
        }
        homefy().getLibrary().search(text, type) { mAdapter?.setSongs(it) }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val type = parent!!.getItemAtPosition(position) as SearchType
        doSearch(mSearch!!.text, type)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // NOthing to see here
    }

    private class TypingListener(val typeSpinner: Spinner,
                                 val cb: (Editable?, SearchType) -> Unit) : TextWatcher {

        override fun afterTextChanged(s: Editable?) {
            cb(s, typeSpinner.selectedItem!! as SearchType)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Not needed
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Not needed
        }

    }
}

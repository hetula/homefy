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

package xyz.hetula.homefy.library

import android.os.Bundle
import android.support.v4.app.Fragment
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
import xyz.hetula.homefy.R
import xyz.hetula.homefy.service.Homefy

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class SongSearchFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private var mSearch: EditText? = null
    private var mAdapter: SongAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_song_search, container, false)

        val mRecycler = root.recyclerView
        mRecycler!!.setHasFixedSize(true)
        mRecycler.layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false)

        root.fast_scroller.setRecyclerView(root.recyclerView)
        root.recyclerView.addOnScrollListener(root.fast_scroller.onScrollListener)

        mAdapter = SongAdapter(Homefy.library().songs)
        mRecycler.adapter = mAdapter

        val adapter = ArrayAdapter<SearchType>(context,
                android.R.layout.simple_spinner_item, SearchType.values())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        root.sp_search.adapter = adapter

        root.txt_search.addTextChangedListener(TypingListener(root.sp_search, this::doSearch))
        mSearch = root.txt_search

        (activity as AppCompatActivity).supportActionBar?.title = context.getString(R.string.nav_search)
        return root
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onPause() {
        super.onPause()
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun doSearch(search: Editable?, type: SearchType) {
        if(search == null ) {
            mAdapter?.setSongs(Homefy.library().songs)
            return
        }
        val text = search.toString()
        if(text.isBlank()) {
            mAdapter?.setSongs(Homefy.library().songs)
            return
        }
        Homefy.library().search(text, type, { mAdapter?.setSongs(it) })
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val type = parent!!.getItemAtPosition(position) as SearchType
        doSearch(mSearch!!.text, type)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // NOthing to see here
    }

    private class TypingListener(val typeSpinner: Spinner,
                                 val cb: (Editable?, SearchType) -> Unit) :TextWatcher {

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

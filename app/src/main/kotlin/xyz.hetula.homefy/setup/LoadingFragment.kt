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

package xyz.hetula.homefy.setup

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import xyz.hetula.homefy.MainFragment
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.Homefy
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class LoadingFragment : Fragment() {
    private var mCount: AtomicInteger? = null
    private var mSongs: MutableList<Song> = ArrayList()
    private var mLoaded: TextView? = null
    private var mSongsTotal: Int = 0
    private var mLoadStarted: Long = 0

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val main = inflater!!.inflate(R.layout.fragment_loading, container, false) as FrameLayout
        mLoaded = main.findViewById(R.id.txt_songs_loaded)
        mSongsTotal = Homefy.protocol().info.databaseSize
        initialize()
        return main
    }

    private fun initialize() {
        mSongs.clear()
        mLoadStarted = System.currentTimeMillis()
        Homefy.protocol().requestPages(250,
                this::fetchData,
                { _ ->
                    Toast.makeText(context,
                            "Error when Connecting!", Toast.LENGTH_LONG).show()
                })

    }

    private fun fetchData(urls: Array<String>) {
        mCount = AtomicInteger(urls.size)
        for (url in urls) {
            Homefy.protocol().request(
                    url,
                    this::onSongs,
                    { _ ->
                        Toast.makeText(context,
                                "Error when Connecting!", Toast.LENGTH_LONG).show()
                    },
                    Array<Song>::class.java)
        }
    }

    @Synchronized private fun ready(): Boolean {
        return mCount!!.decrementAndGet() == 0
    }

    private fun onSongs(songs: Array<Song>) {
        mSongs.addAll(Arrays.asList(*songs))
        mLoaded!!.text = context.resources
                .getQuantityString(R.plurals.songs_loaded, mSongs.size, mSongs.size, mSongsTotal)

        if (ready()) {
            val time = System.currentTimeMillis() - mLoadStarted
            Log.d("LoadingFragment", "Songs loaded in $time ms")

            initializeHomefy()
        }
    }

    private fun initializeHomefy() {
        val songs = mSongs
        mSongs = ArrayList() // Create new list so old one can't be used here
        // initialize will use given list and does not create new one.
        Homefy.library().initialize(songs)
        fragmentManager
                .beginTransaction()
                .replace(R.id.container, MainFragment())
                .commit()
    }
}

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
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_songs.view.*
import xyz.hetula.homefy.HomefyFragment
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.Song
import java.util.*
import java.util.stream.Collectors

class SongsFragment : HomefyFragment() {
    private lateinit var mTitleText: TextView
    private lateinit var mSubTitleText: TextView
    private lateinit var mSongCount: TextView
    private lateinit var mSongsDuration: TextView
    private lateinit var mSongList: RecyclerView

    private var mSongs = Collections.emptyList<Song>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_songs, container, false)
        mTitleText = root.titleText
        mSubTitleText = root.subTitleText
        mSongCount = root.songCount
        mSongsDuration = root.songsDuration
        mSongList = root.songList

        mTitleText.text = arguments?.getString(ARGUMENT_TITLE)
        mSubTitleText.text = arguments?.getString(ARGUMENT_SUBTITLE)
        parseSongsFromArgs()

        mSongCount.text = context!!.resources.getQuantityString(R.plurals.song_count, mSongs.size, mSongs.size)
        val duration: Long = mSongs.map { it.length }.sum()
        mSongsDuration.text = DateUtils.formatElapsedTime(duration)

        mSongList.layoutManager = LinearLayoutManager(context)
        val adapter = SongAdapter(mSongs, homefy().getPlayer(), homefy().getPlaylists())
        adapter.onSongPlay = {
            LibraryFragment.openPlaying = true
            fragmentManager!!.popBackStack()
        }
        mSongList.adapter = adapter
        return root
    }

    private fun parseSongsFromArgs() {
        val songIds = arguments?.getStringArray(ARGUMENT_SONGS)!!.toList()
        mSongs = songIds.stream()
                .map { homefy().getLibrary().getSongById(it) }
                .filter { it != null }
                .collect(Collectors.toList())
    }

    companion object {
        const val ARGUMENT_TITLE = "SongsFragment.ARGUMENT_TITLE"
        const val ARGUMENT_SUBTITLE = "SongsFragment.ARGUMENT_SUBTITLE"
        const val ARGUMENT_SONGS = "SongsFragment.ARGUMENT_SONGS"
    }
}

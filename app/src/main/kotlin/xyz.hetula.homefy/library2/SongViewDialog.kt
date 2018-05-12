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

package xyz.hetula.homefy.library2

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_song_view.*
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.service.HomefyService

class SongViewDialog(context: Context,
                     private val homefy: HomefyService,
                     private val title: String,
                     private val subtitle: String,
                     private val songs: List<Song>) : Dialog(context, R.style.SongDialog) {
    private lateinit var mTitleText: TextView
    private lateinit var mSubTitleText: TextView
    private lateinit var mSongCount: TextView
    private lateinit var mSongsDuration: TextView
    private lateinit var mSongList: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.dialog_song_view)
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT)

        mTitleText = titleText
        mSubTitleText = subTitleText
        mSongCount = songCount
        mSongsDuration = songsDuration
        mSongList = songList

        mTitleText.text = title
        mSubTitleText.text = subtitle
        mSongCount.text = context.resources.getQuantityText(R.plurals.song_count, songs.size)
        val duration: Long = songs.map { it.length }.sum()
        mSongsDuration.text = DateUtils.formatElapsedTime(duration)

        mSongList.layoutManager = LinearLayoutManager(context)
        mSongList.adapter = SongAdapter2(songs, homefy.getPlayer(), homefy.getPlaylists())
    }
}

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

package xyz.hetula.homefy.player

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import xyz.hetula.homefy.R
import xyz.hetula.homefy.service.Homefy
import java.util.*

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class PlayerFragment : Fragment() {
    private val mPlaybackListener = { _: Song, _: Int -> updateSongInfo() }
    private var mTxtTitle: TextView? = null
    private var mTxtArtist: TextView? = null
    private var mTxtAlbum: TextView? = null
    private var mBtnPausePlay: ImageButton? = null


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val main = inflater!!.inflate(R.layout.fragment_player, container, false) as LinearLayout
        mTxtTitle = main.findViewById(R.id.txt_song_title) as TextView
        mTxtArtist = main.findViewById(R.id.txt_song_artist) as TextView
        mTxtAlbum = main.findViewById(R.id.txt_song_album) as TextView
        mBtnPausePlay = main.findViewById(R.id.btn_play_pause) as ImageButton
        val btnPlayback = main.findViewById(R.id.btn_playback) as ImageButton
        val btnStop = main.findViewById(R.id.btn_stop)
        val btnNext = main.findViewById(R.id.btn_next)
        val btnPrevious = main.findViewById(R.id.btn_previous)

        btnStop.setOnClickListener { v -> Homefy.player().stop() }
        btnNext.setOnClickListener { v ->
            Homefy.player().next()
            updateSongInfo()
        }
        mBtnPausePlay!!.setOnClickListener { v -> Homefy.player().pauseResume() }

        return main
    }

    override fun onResume() {
        super.onResume()
        updateSongInfo()
        Homefy.player().registerPlaybackListener(mPlaybackListener)
    }

    override fun onPause() {
        super.onPause()
        Homefy.player().unregisterPlaybackListener(mPlaybackListener)
    }

    private fun updateSongInfo() {
        val now = Homefy.player().nowPlaying()
        if (now != null) {
            mTxtTitle!!.text = String.format(Locale.getDefault(),
                    "%d - %s", now.track, now.title)
            mTxtArtist!!.text = now.artist
            mTxtAlbum!!.text = now.album
            if (Homefy.player().isPaused) {
                mBtnPausePlay!!.setImageResource(R.drawable.ic_play_circle)
            } else if (Homefy.player().isPlaying) {
                mBtnPausePlay!!.setImageResource(R.drawable.ic_pause_circle)
            }
        } else {
            mTxtTitle!!.text = null
            mTxtArtist!!.text = null
            mTxtAlbum!!.text = null
            mBtnPausePlay!!.setImageResource(R.drawable.ic_play_circle)
        }

    }
}

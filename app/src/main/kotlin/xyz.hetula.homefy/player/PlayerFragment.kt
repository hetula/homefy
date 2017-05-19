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
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
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

        btnStop.setOnClickListener { _ -> Homefy.player().stop() }
        btnNext.setOnClickListener { _ ->
            Homefy.player().next()
            updateSongInfo()
        }
        btnPrevious.setOnClickListener({ _ ->
            Homefy.player().previous()
            updateSongInfo()
        })
        mBtnPausePlay!!.setOnClickListener { _ -> Homefy.player().pauseResume() }
        btnPlayback.setOnClickListener(this::onPlaybackModeClick)

        return main
    }

    override fun onResume() {
        super.onResume()
        updateSongInfo()
        (activity as AppCompatActivity).supportActionBar?.hide()
        Homefy.player().registerPlaybackListener(mPlaybackListener)
    }

    override fun onPause() {
        super.onPause()
        Homefy.player().unregisterPlaybackListener(mPlaybackListener)
    }

    private fun updateSongInfo() {
        val now = Homefy.player().nowPlaying()
        if (now != null) {
            if (now.track >= 0)
                mTxtTitle!!.text = String.format(Locale.getDefault(),
                        "%d - %s", now.track, now.title)
            else
                mTxtTitle!!.text = now.title

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

    private fun onPlaybackModeClick(v: View) {
        val button = v as ImageButton
        val mode = Homefy.player().cyclePlaybackMode()
        val imgRes: Int
        when(mode) {
            PlaybackMode.NORMAL -> imgRes = R.drawable.ic_repeat_off
            PlaybackMode.REPEAT -> imgRes = R.drawable.ic_repeat
            PlaybackMode.REPEAT_SINGLE -> imgRes = R.drawable.ic_repeat_one
            PlaybackMode.RANDOM -> imgRes = R.drawable.ic_shuffle
        }
        button.setImageDrawable(ContextCompat.getDrawable(context, imgRes))
    }
}

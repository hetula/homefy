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

package xyz.hetula.homefy.player

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_player.view.*
import xyz.hetula.homefy.HomefyFragment
import xyz.hetula.homefy.R
import xyz.hetula.homefy.Utils
import xyz.hetula.homefy.playlist.PlaylistDialog
import xyz.hetula.homefy.service.HomefyService
import java.util.*

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class PlayerFragment : HomefyFragment() {
    private val mPlaybackListener = this::onSongUpdate

    private lateinit var mTxtTitle: TextView
    private lateinit var mTxtArtist: TextView
    private lateinit var mTxtAlbum: TextView
    private lateinit var mTxtLength: TextView
    private lateinit var mTxtBuffering: TextView
    private lateinit var mBtnPausePlay: ImageButton
    private lateinit var mBtnFavorite: ImageButton
    private lateinit var mSeekBar: SeekBar

    private val mPositionLoop = Handler()
    private val mUpdateRunnable = this::posQuery
    private var mIsShowing = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val main = inflater.inflate(R.layout.fragment_player, container, false) as LinearLayout
        mTxtTitle = main.txt_song_title!!
        mTxtArtist = main.txt_song_artist!!
        mTxtAlbum = main.txt_song_album!!
        mTxtLength = main.txt_song_length!!
        mTxtBuffering = main.txt_buffering!!
        mSeekBar = main.seek_song_length!!
        mBtnPausePlay = main.btn_play_pause!!
        mBtnFavorite = main.btn_favorite!!

        mBtnPausePlay.setOnClickListener { _ -> homefy().getPlayer().pauseResume() }

        main.btn_stop!!.setOnClickListener { _ -> homefy().getPlayer().stop() }
        main.btn_next!!.setOnClickListener { _ ->
            homefy().getPlayer().next()
            val song = homefy().getPlayer().nowPlaying()
            if (song != null) {
                updateSongInfo(song)
            }
        }
        main.btn_previous.setOnClickListener({ _ ->
            homefy().getPlayer().previous()
            val song = homefy().getPlayer().nowPlaying()
            if (song != null) {
                updateSongInfo(song)
            }
        })
        main.btn_favorite.setOnClickListener {
            val song = homefy().getPlayer().nowPlaying() ?: return@setOnClickListener
            homefy().getPlaylists().favorites.toggle(homefy().getPlaylists(), song)
            updateFavIco(song)
        }
        main.btn_add_to_playlist.setOnClickListener {
            val song = homefy().getPlayer().nowPlaying() ?: return@setOnClickListener
            PlaylistDialog.addToPlaylist(context!!, song, homefy().getPlaylists()) {
                Snackbar.make(main, R.string.playlist_dialog_added,
                        Snackbar.LENGTH_SHORT).show()
            }
        }
        main.btn_shutdown.setOnClickListener {
            doShutdown()
        }
        main.btn_download.setOnClickListener {
            (activity as PlayerActivity).download(homefy().getPlayer().nowPlaying())
        }
        main.seek_song_length.setOnSeekBarChangeListener(SeekListener(this))
        main.btn_playback!!.setOnClickListener(this::onPlaybackModeClick)
        return main
    }

    override fun onResume() {
        super.onResume()
        mIsShowing = true
        val song = homefy().getPlayer().nowPlaying()
        if (song != null) {
            updateSongInfo(song)
        }
        homefy().getPlayer().registerPlaybackListener(mPlaybackListener)
        mPositionLoop.post(mUpdateRunnable)
        // Enable title scrolling...
        mTxtTitle.isSelected = true
    }

    override fun onPause() {
        super.onPause()
        mIsShowing = false
        Log.d(TAG, "Paused!")
        homefy().getPlayer().unregisterPlaybackListener(mPlaybackListener)
        mPositionLoop.removeCallbacks(mUpdateRunnable)
        mTxtBuffering.visibility = View.INVISIBLE
    }

    private fun onSongUpdate(song: Song?, state: Int, param: Int) {
        when (state) {
            HomefyPlayer.STATE_BUFFERING -> onBuffer(param)
            HomefyPlayer.STATE_PLAY -> onDurUpdate(false)
            HomefyPlayer.STATE_PAUSE -> onDurUpdate(true)
            HomefyPlayer.STATE_STOP -> clear()
            HomefyPlayer.STATE_RESUME -> onDurUpdate(false)
        }
        if (song != null && state != HomefyPlayer.STATE_BUFFERING) {
            updateSongInfo(song)
        }
    }

    private fun onBuffer(buffered: Int) {
        Log.d(TAG, "Buffering $buffered")
        if (buffered >= 100) {
            mTxtBuffering.visibility = View.INVISIBLE
            mTxtBuffering.text = getString(R.string.buffering, 0)
        } else {
            mTxtBuffering.visibility = View.VISIBLE
            mTxtBuffering.text = getString(R.string.buffering, buffered)
        }
    }

    private fun onDurUpdate(paused: Boolean) {
        if (!mIsShowing) return
        if (paused) {
            mPositionLoop.removeCallbacks(mUpdateRunnable)
        } else {
            mPositionLoop.post(mUpdateRunnable)
        }
    }

    private fun clear() {
        mBtnPausePlay.setImageResource(R.drawable.ic_play_circle)
        mTxtLength.text = Utils.parseTime(0, 0)
    }

    private fun updateSongInfo(now: Song) {
        if (now.track >= 0) {
            mTxtTitle.text = String.format(Locale.getDefault(),
                    "%d - %s", now.track, now.title)
        } else {
            mTxtTitle.text = now.title
        }
        mTxtTitle.isSelected = true

        val position = homefy().getPlayer().queryPosition().toLong()

        mTxtArtist.text = now.artist
        mTxtAlbum.text = now.album
        mTxtLength.text = Utils.parseTime(position, now.length)

        if (homefy().getPlayer().isPaused) {
            mBtnPausePlay.setImageResource(R.drawable.ic_play_circle)
        } else if (homefy().getPlayer().isPlaying) {
            mBtnPausePlay.setImageResource(R.drawable.ic_pause_circle)
        }
        updateFavIco(now)
    }

    private fun updateFavIco(song: Song) {
        if (homefy().getPlaylists().isFavorite(song)) {
            mBtnFavorite.setImageResource(R.drawable.ic_favorite_large)
        } else {
            mBtnFavorite.setImageResource(R.drawable.ic_not_favorite_large)
        }
    }

    private fun posQuery() {
        if(!mIsShowing) {
            return
        }
        val song = homefy().getPlayer().nowPlaying()
        var pos: Long
        val dur: Long
        if (song != null) {
            pos = homefy().getPlayer().queryPosition().toLong()
            dur = song.length
        } else {
            pos = 0
            dur = 0
        }
        if (pos > dur) {
            pos = dur
        }
        mSeekBar.max = dur.toInt()
        mSeekBar.progress = pos.toInt()
        mTxtLength.text = Utils.parseTime(pos, dur)
        if (song != null && mIsShowing) {
            mPositionLoop.postDelayed(mUpdateRunnable, 750)
        }
    }

    private fun onPlaybackModeClick(v: View) {
        val button = v as ImageButton
        val mode = homefy().getPlayer().cyclePlaybackMode()
        val imgRes: Int
        imgRes = when (mode) {
            PlaybackMode.NORMAL -> R.drawable.ic_repeat_off
            PlaybackMode.REPEAT -> R.drawable.ic_repeat
            PlaybackMode.REPEAT_SINGLE -> R.drawable.ic_repeat_one
            PlaybackMode.RANDOM -> R.drawable.ic_shuffle
        }
        button.setImageDrawable(ContextCompat.getDrawable(context!!, imgRes))
    }

    private fun doShutdown() {
        Log.d(TAG, "Shutting down!")
        val context = activity!!.applicationContext
        context.stopService(Intent(context, HomefyService::class.java))
        activity!!.finishAffinity()
    }

    private class SeekListener(val player: PlayerFragment) : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (!fromUser) {
                return
            }
            player.homefy().getPlayer().seekTo(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            player.mPositionLoop.removeCallbacks(player.mUpdateRunnable)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            player.mPositionLoop.post(player.mUpdateRunnable)
        }
    }

    companion object {
        const val TAG = "PlayerFragment"
    }
}

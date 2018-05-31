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

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_player.view.*
import xyz.hetula.homefy.R
import xyz.hetula.homefy.Utils
import xyz.hetula.homefy.playlist.FavoriteChangeListener
import xyz.hetula.homefy.playlist.PlaylistDialog
import xyz.hetula.homefy.service.HomefyService
import java.util.*

class PlayerView : FrameLayout, FavoriteChangeListener {
    private val mPlaybackListener = this::onSongUpdate

    private val mTxtTitle: TextView
    private val mTxtArtist: TextView
    private val mTxtAlbum: TextView
    private val mTxtLength: TextView
    private val mTxtBuffering: TextView
    private val mBtnPausePlay: ImageButton
    private val mBtnFavorite: ImageButton
    private val mBtnStop: ImageButton
    private val mBtnNext: ImageButton
    private val mBtnPrevious: ImageButton
    private val mBtnAddToPlaylist: ImageButton
    private val mBtnShutdown: ImageButton
    private val mBtnDownload: ImageButton
    private val mBtnPlayback: ImageButton
    private val mSeekBar: SeekBar
    private val mSongIconView: ImageView

    private val mPositionLoop = Handler()
    private val mUpdateRunnable = this::posQuery
    private var mIsShowing = false

    private lateinit var mHomefyService: HomefyService

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val inflater = LayoutInflater.from(context)
        val root = inflater.inflate(R.layout.fragment_player, this, true)
        mTxtTitle = root.txt_song_title
        mTxtArtist = root.txt_song_artist
        mTxtAlbum = root.txt_song_album
        mTxtLength = root.txt_song_length
        mTxtBuffering = root.txt_buffering
        mSeekBar = root.seek_song_length
        mBtnPausePlay = root.btn_play_pause
        mBtnFavorite = root.btn_favorite
        mSongIconView = root.song_icon_view
        mBtnStop = root.btn_stop
        mBtnNext = root.btn_next
        mBtnPrevious = root.btn_previous
        mBtnAddToPlaylist = root.btn_add_to_playlist
        mBtnShutdown = root.btn_shutdown
        mBtnDownload = root.btn_download
        mBtnPlayback = root.btn_playback

        mBtnPlayback.setOnClickListener(this::onPlaybackModeClick)
        mSeekBar.setOnSeekBarChangeListener(PlayerView.SeekListener(this))
        mBtnShutdown.setOnClickListener { doShutdown() }
        mBtnAddToPlaylist.setOnClickListener {
            val song = homefy().getPlayer().nowPlaying() ?: return@setOnClickListener
            PlaylistDialog.addToPlaylist(context, song, homefy().getPlaylists()) {
                Snackbar.make(this, R.string.playlist_dialog_added,
                        Snackbar.LENGTH_SHORT).show()
            }
        }
        mBtnFavorite.setOnClickListener {
            val song = homefy().getPlayer().nowPlaying() ?: return@setOnClickListener
            homefy().getPlaylists().favorites.toggle(context, homefy().getPlaylists(), song)
            updateFavIco(song)
        }
        mBtnPrevious.setOnClickListener({ _ ->
            homefy().getPlayer().previous()
            val song = homefy().getPlayer().nowPlaying()
            if (song != null) {
                updateSongInfo(song)
            }
        })
        mBtnNext.setOnClickListener { _ ->
            homefy().getPlayer().next()
            val song = homefy().getPlayer().nowPlaying()
            if (song != null) {
                updateSongInfo(song)
            }
        }
        mBtnStop.setOnClickListener { _ -> homefy().getPlayer().stop() }
        mBtnPausePlay.setOnClickListener { _ -> homefy().getPlayer().pauseResume() }
    }

    fun setHomefy(homefyService: HomefyService) {
        mHomefyService = homefyService
    }

    fun setOnDownloadClick(click: (View) -> Unit) {
        mBtnDownload.setOnClickListener(click)
    }

    internal fun homefy() = mHomefyService

    fun show() {
        mIsShowing = true
        val song = homefy().getPlayer().nowPlaying()
        if (song != null) {
            updateSongInfo(song)
            updateSongIcon(song)
        }
        homefy().getPlayer().registerPlaybackListener(mPlaybackListener)
        mPositionLoop.post(mUpdateRunnable)
        // Enable title scrolling...
        mTxtTitle.isSelected = true
        visibility = View.VISIBLE
    }

    fun hide() {
        visibility = View.GONE
        mIsShowing = false
        homefy().getPlayer().unregisterPlaybackListener(mPlaybackListener)
        mPositionLoop.removeCallbacks(mUpdateRunnable)
        mTxtBuffering.visibility = View.INVISIBLE
    }

    override fun onFavoriteChanged(song: Song) {
        val current = homefy().getPlayer().nowPlaying() ?: return
        if(song.id == current.id) {
            updateFavIco(song)
        }
    }

    private fun onSongUpdate(song: Song?, state: Int, param: Int) {
        when (state) {
            HomefyPlayer.STATE_BUFFERING -> onBuffer(param)
            HomefyPlayer.STATE_PLAY -> onDurUpdate(false)
            HomefyPlayer.STATE_PAUSE -> onDurUpdate(true)
            HomefyPlayer.STATE_STOP -> clear()
            HomefyPlayer.STATE_RESUME -> onDurUpdate(false)
        }
        if (song == null) {
            mSongIconView.setImageResource(R.drawable.ic_music)
            return
        }
        if (state != HomefyPlayer.STATE_BUFFERING) {
            updateSongInfo(song)
        }
        updateSongIcon(song)
    }

    private fun updateSongIcon(song: Song) {
        if (song.albumArt == null) {
            mSongIconView.setImageResource(R.drawable.ic_music)
        } else {
            mSongIconView.setImageBitmap(song.albumArt)
        }
    }

    private fun onBuffer(buffered: Int) {
        Log.d(TAG, "Buffering $buffered")
        if (buffered >= 100) {
            mTxtBuffering.visibility = View.INVISIBLE
            mTxtBuffering.text = context.getString(R.string.player_buffering, 0)
        } else {
            mTxtBuffering.visibility = View.VISIBLE
            mTxtBuffering.text = context.getString(R.string.player_buffering, buffered)
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
        if (!mIsShowing) {
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
        val context = context.applicationContext
        context.stopService(Intent(context, HomefyService::class.java))
        //activity!!.finishAffinity()
    }

    private class SeekListener(val player: PlayerView) : SeekBar.OnSeekBarChangeListener {
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
        private const val TAG = "PlayerView"
    }
}

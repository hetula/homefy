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

package xyz.hetula.homefy.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import xyz.hetula.homefy.HomefyActivity
import xyz.hetula.homefy.R
import xyz.hetula.homefy.library.HomefyLibrary
import xyz.hetula.homefy.player.HomefyPlayer
import xyz.hetula.homefy.player.PlayerActivity
import xyz.hetula.homefy.player.Song
import xyz.hetula.homefy.playlist.HomefyPlaylist
import xyz.hetula.homefy.service.protocol.HomefyProtocol

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class HomefyService : Service() {
    private val mBinder = HomefyBinder(this)
    private val homefyNotificationId = "homefy_notification"

    private lateinit var mProtocol: HomefyProtocol
    private lateinit var mLibrary: HomefyLibrary
    private lateinit var mPlayer: HomefyPlayer
    private lateinit var mPlaylists: HomefyPlaylist

    private val mPlaybackListener = { _: Song?, state: Int, _: Int -> onPlay(state) }
    private var mSession: MediaSessionCompat? = null

    override fun onBind(intent: Intent): IBinder? = mBinder

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action != null) {
            when (intent.action) {
                CLOSE_INTENT -> closeApp()
                FAV_INTENT -> favoriteCurrentSong()
                INIT_COMPLETE -> initialize()
                else -> {
                    val mediaSession = mSession ?: return Service.START_NOT_STICKY
                    MediaButtonReceiver.handleIntent(mediaSession, intent)
                }
            }
            return Service.START_NOT_STICKY
        }
        return Service.START_STICKY
    }

    override fun onCreate() {
        Log.d(TAG, "Creating HomefyService")
        super.onCreate()
        createChannel()

        mProtocol = ServiceInitializer.protocol(Unit)
        mLibrary = ServiceInitializer.library(mProtocol)
        mPlayer = ServiceInitializer.player(mProtocol, mLibrary)
        mPlaylists = ServiceInitializer.playlist(Unit)

        mProtocol.initialize(applicationContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Destroying Homefy Service")
        destroyChannel()

        mPlayer.unregisterPlaybackListener(mPlaybackListener)
        mProtocol.release()
        mLibrary.release()
        mPlayer.release(applicationContext)
        mSession = null

        stopForeground(true)
    }

    fun getProtocol() = mProtocol

    fun getLibrary() = mLibrary

    fun getPlayer() = mPlayer

    fun getPlaylists() = mPlaylists

    private fun initialize() {
        Log.d(TAG, "Initializing HomefyService")
        val session = mPlayer.initalize(applicationContext)
        mSession = session

        createNotification(session)
        mPlayer.registerPlaybackListener(mPlaybackListener)
    }

    private fun createChannel() {
        val homefyChannel = NotificationChannel(homefyNotificationId,
                getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT)
        homefyChannel.description = getString(R.string.channel_desc)
        homefyChannel.enableLights(true)
        homefyChannel.enableVibration(false)
        homefyChannel.setShowBadge(false)
        homefyChannel.setSound(null, null)
        homefyChannel.lightColor = Color.BLUE
        homefyChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        val notificationMngr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationMngr.createNotificationChannel(homefyChannel)
    }

    private fun destroyChannel() {
        val notificationMngr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationMngr.deleteNotificationChannel(homefyNotificationId)
    }

    private fun closeApp() {
        applicationContext.sendBroadcast(Intent(HomefyActivity.KILL_INTENT))
        stopSelf()
    }

    private fun createNotification(mediaSession: MediaSessionCompat) {
        startForeground(NOTIFICATION_ID, setupNotification(mediaSession))
    }

    private fun updateNotification(mediaSession: MediaSessionCompat) {
        val nM = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nM.notify(NOTIFICATION_ID, setupNotification(mediaSession))
    }

    private fun favoriteCurrentSong() {
        val mediaSession = mSession ?: return
        val song = mPlayer.nowPlaying() ?: return
        mPlaylists.favorites.toggle(mPlaylists, song)
        updateNotification(mediaSession)
    }

    private fun setupNotification(mediaSession: MediaSessionCompat): Notification {
        val song = mPlayer.nowPlaying()
        val builder = NotificationCompat.Builder(applicationContext, homefyNotificationId)
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_music_notification)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setOngoing(true)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (song == null) {
            builder.setContentTitle(getString(R.string.app_desc))
                    .addAction(R.drawable.ic_close_notify, "Close", closeIntent())
            return builder.build()
        }

        val favDrawable = if (mPlaylists.isFavorite(song)) {
            R.drawable.ic_favorite
        } else {
            R.drawable.ic_not_favorite_notification
        }

        val playDrawable: Int
        val playDesc: String
        if (mPlayer.isPaused) {
            playDrawable = R.drawable.ic_play_notification
            playDesc = "Play"
        } else {
            playDrawable = R.drawable.ic_pause_notification
            playDesc = "Pause"
        }

        val playPauseIntent = MediaButtonReceiver
                .buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY)
        val previousIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        val nextIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT)

        val largeIcon = song.albumArt
                ?: BitmapFactory.decodeResource(resources, R.drawable.ic_album_big)
        builder.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                PlaybackStateCompat.ACTION_STOP))
                .addAction(R.drawable.ic_close_notify, "Close", closeIntent())
                .addAction(R.drawable.ic_skip_previous_notification, "Previous", previousIntent)
                .addAction(playDrawable, playDesc, playPauseIntent)
                .addAction(R.drawable.ic_skip_next_notification, "Next", nextIntent)
                .addAction(favDrawable, "Favorite", favIntent())
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(2, 3, 4))
                .setColorized(true)
                .setLargeIcon(largeIcon)
                .setContentIntent(contentIntent())
                .setContentTitle(song.title)
                .setContentText(song.album)
                .setSubText(song.artist)

        return builder.build()
    }

    private fun contentIntent(): PendingIntent {
        val launchMe = Intent(this, PlayerActivity::class.java)
        launchMe.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS

        val taskStack = TaskStackBuilder.create(this)
        taskStack.addParentStack(PlayerActivity::class.java)
        taskStack.addNextIntent(launchMe)
        return taskStack.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)!!
    }

    private fun closeIntent(): PendingIntent {
        val close = Intent(this, HomefyService::class.java)
        close.action = CLOSE_INTENT
        return PendingIntent.getService(
                this,
                42,
                close,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun favIntent(): PendingIntent {
        val fav = Intent(this, HomefyService::class.java)
        fav.action = FAV_INTENT
        return PendingIntent.getService(
                this,
                0x42,
                fav,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun onPlay(state: Int) {
        if (state == HomefyPlayer.STATE_PLAY ||
                state == HomefyPlayer.STATE_PAUSE ||
                state == HomefyPlayer.STATE_RESUME) {
            val mediaSession = mSession ?: return
            updateNotification(mediaSession)
        }
    }

    internal class HomefyBinder(private val service: HomefyService) : Binder() {
        fun getService() = service
    }

    companion object {
        const val CLOSE_INTENT = "xyz.hetula.homefy.service.CLOSE"
        const val FAV_INTENT = "xyz.hetula.homefy.service.FAVORITE"
        const val INIT_COMPLETE = "xyz.hetula.homefy.service.INIT_COMPLETE"

        private const val TAG = "HomefyService"
        private const val NOTIFICATION_ID = 444
    }
}

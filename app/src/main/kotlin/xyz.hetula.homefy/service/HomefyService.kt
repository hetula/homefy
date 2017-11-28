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

package xyz.hetula.homefy.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
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
import xyz.hetula.homefy.service.protocol.DefaultHomefyProtocol
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

    private val mPlaybackListener = this::onPlay
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

        mProtocol = DefaultHomefyProtocol()
        mLibrary = HomefyLibrary(mProtocol)
        mPlayer = HomefyPlayer(mProtocol, mLibrary)
        mPlaylists = HomefyPlaylist()

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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val homefyChannel = NotificationChannel(homefyNotificationId,
                getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT)
        homefyChannel.description = "Homefy Music Player"
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
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
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_album_big)
        val builder = NotificationCompat.Builder(applicationContext, homefyNotificationId)
        builder.setLargeIcon(largeIcon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.ic_music_notification)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setColorized(true)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setOngoing(true)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (song == null) {
            builder.setContentTitle(getString(R.string.app_name))
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

                .setContentIntent(contentIntent())
                .setContentTitle(song.title)
                .setContentText("${song.artist} - ${song.album}")

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
        return taskStack.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
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

    private fun onPlay(song: Song?, state: Int, param: Int) {
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
        val CLOSE_INTENT = "xyz.hetula.homefy.service.CLOSE"
        val FAV_INTENT = "xyz.hetula.homefy.service.FAVORITE"
        val INIT_COMPLETE = "xyz.hetula.homefy.service.INIT_COMPLETE"

        private val TAG = "HomefyService"
        private val NOTIFICATION_ID = 444
    }
}

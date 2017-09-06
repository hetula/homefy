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
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import xyz.hetula.homefy.HomefyActivity
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.HomefyPlayer
import xyz.hetula.homefy.player.PlayerActivity
import xyz.hetula.homefy.player.Song

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class HomefyService : Service() {
    private val HOMEFY_NOTIFICATION_ID = "homefy_notification"
    private val mPlaybackListener = this::onPlay
    private var mSession: MediaSessionCompat? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == CLOSE_INTENT) {
            closeApp()
            return Service.START_NOT_STICKY
        }
        if (intent.action == FAV_INTENT) {
            favoriteCurrentSong()
            return Service.START_NOT_STICKY
        }

        if (mSession != null) {
            MediaButtonReceiver.handleIntent(mSession, intent)
        }
        if (isReady) return Service.START_STICKY
        isReady = true
        createChannel()

        Log.d(TAG, "Starting HomefyService")
        Homefy.initialize(applicationContext)


        createNotification()
        Homefy.player().registerPlaybackListener(mPlaybackListener)
        mSession = Homefy.player().mSession

        return Service.START_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val homefyChannel = NotificationChannel(HOMEFY_NOTIFICATION_ID,
                getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
        homefyChannel.description = "Music player"
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
        notificationMngr.deleteNotificationChannel(HOMEFY_NOTIFICATION_ID)
    }

    private fun closeApp() {
        applicationContext.sendBroadcast(Intent(HomefyActivity.KILL_INTENT))
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Destroying Homefy Service")
        destroyChannel()
        isReady = false
        Homefy.player().unregisterPlaybackListener(mPlaybackListener)
        Homefy.destroy()
        stopForeground(true)
        stopSelf()
        // Kill Process, just in case.
        Process.killProcess(Process.myPid())
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotification() {
        startForeground(NOTIFICATION_ID, setupNotification())
    }

    private fun updateNotification() {
        val nM = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nM.notify(NOTIFICATION_ID, setupNotification())
    }

    private fun favoriteCurrentSong() {
        val song = Homefy.player().nowPlaying() ?: return
        Homefy.playlist().favorites.toggle(song)
        updateNotification()
    }

    private fun setupNotification(): Notification {
        val song = Homefy.player().nowPlaying()
        val mediaSession = Homefy.player().mSession!!
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_album_big)
        val builder = NotificationCompat.Builder(applicationContext, HOMEFY_NOTIFICATION_ID)
        builder.setLargeIcon(largeIcon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_music_notification)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setColorized(true)
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setOngoing(true)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (song == null) {
            builder.setContentTitle(getString(R.string.app_name))
            return builder.build()
        }

        val favDrawable: Int
        favDrawable = if (Homefy.playlist().isFavorite(song)) {
            R.drawable.ic_favorite
        } else {
            R.drawable.ic_not_favorite_notification
        }

        val playDrawable: Int
        val playDesc: String
        if (Homefy.player().isPaused) {
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
            updateNotification()
        }
    }

    companion object {
        val CLOSE_INTENT = "xyz.hetula.homefy.service.CLOSE"
        val FAV_INTENT = "xyz.hetula.homefy.service.FAVORITE"

        private val TAG = "HomefyService"
        private val NOTIFICATION_ID = 444

        var isReady = false
            private set
    }
}

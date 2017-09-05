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

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.os.Process
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import xyz.hetula.homefy.HomefyActivity
import xyz.hetula.homefy.MainActivity
import xyz.hetula.homefy.R
import xyz.hetula.homefy.player.HomefyPlayer
import xyz.hetula.homefy.player.PlayerActivity
import xyz.hetula.homefy.player.Song
import java.io.File

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

        if (mSession != null) {
            MediaButtonReceiver.handleIntent(mSession, intent)
        }
        if (isReady) return Service.START_STICKY
        isReady = true

        Log.d(TAG, "Starting HomefyService")
        Homefy.initialize(applicationContext)

        createNotification()
        Homefy.player().registerPlaybackListener(mPlaybackListener)
        mSession = Homefy.player().mSession

        return Service.START_STICKY
    }

    private fun closeApp() {
        applicationContext.sendBroadcast(Intent(HomefyActivity.KILL_INTENT))
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Destroying Homefy Service")
        val folder = getAndCreateBase()
        if (folder != null) {
            Homefy.playlist().save(folder)
        }
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

    private fun getAndCreateBase(): File? {
        val specific = Homefy.protocol().serverId
        val base = File(applicationContext.filesDir, specific)
        base.mkdir()
        return base
    }

    private fun createNotification() {
        startForeground(NOTIFICATION_ID, setupNotification())
    }

    private fun updateNotification() {
        val nM = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nM.notify(NOTIFICATION_ID, setupNotification())
    }

    private fun setupNotification(): Notification {
        val song = Homefy.player().nowPlaying()
        val mediaSession = Homefy.player().mSession!!
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_album_big)
        val builder = NotificationCompat.Builder(applicationContext, HOMEFY_NOTIFICATION_ID)
        builder.setLargeIcon(largeIcon)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_music_notification)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setOngoing(true)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(contentIntent())
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP))

        if (song != null) {
            val img: Int
            val str: String
            if (Homefy.player().isPaused) {
                img = R.drawable.ic_play_notification
                str = "Play"
            } else {
                img = R.drawable.ic_pause_notification
                str = "Pause"
            }

            builder.addAction(android.support.v4.app.NotificationCompat.Action(
                    img, str,
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                            PlaybackStateCompat.ACTION_PLAY)))
                    .addAction(android.support.v4.app.NotificationCompat.Action(
                            R.drawable.ic_skip_next_notification, "Next",
                            MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT)))
                    .addAction(android.support.v4.app.NotificationCompat.Action(
                            R.drawable.ic_close_notify, "Close",
                            closeIntent()))
                    .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.sessionToken)
                            .setShowActionsInCompactView(0, 1, 2))
                    .setContentTitle(song.title)
                    .setContentText("${song.artist} - ${song.album}")
        } else {
            builder.setContentTitle("Homefy")
                    .setContentText("Nothing is playing")
        }

        return builder.build()
    }

    private fun contentIntent(): PendingIntent {
        val launchMe = Intent(this, PlayerActivity::class.java)
        launchMe.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS

        val taskStack = TaskStackBuilder.create(baseContext)
        taskStack.addParentStack(MainActivity::class.java)
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

    private fun onPlay(song: Song?, state: Int, param: Int) {
        if (state == HomefyPlayer.STATE_PLAY ||
                state == HomefyPlayer.STATE_PAUSE ||
                state == HomefyPlayer.STATE_RESUME) {
            updateNotification()
        }
    }

    companion object {
        val CLOSE_INTENT = "xyz.hetula.homefy.service.CLOSE"

        private val TAG = "HomefyService"
        private val NOTIFICATION_ID = 444

        var isReady = false
            private set
    }
}

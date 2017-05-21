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

import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import xyz.hetula.homefy.R

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class HomefyService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (isReady) return Service.START_STICKY
        isReady = true

        Log.d(TAG, "Starting HomefyService")
        Homefy.initialize(applicationContext)

        createNotification()

        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Destroying Homefy Service")
        isReady = false
        Homefy.destroy()
        stopForeground(true)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotification() {
        val mediaSession = Homefy.player().mSession
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_album_big)
        val builder = NotificationCompat.Builder(applicationContext)
        builder.setContentTitle("Test Title")
                .setContentText("Test Text")
                .setSubText("Test SubText")
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_music)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setOngoing(true)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setContentIntent(mediaSession!!.controller.sessionActivity)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP))
                .addAction(NotificationCompat.Action(
                        R.drawable.ic_pause_circle, "Pause",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                .setStyle(android.support.v7.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0))

        startForeground(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private val TAG = "HomefyService"
        private val NOTIFICATION_ID = 444

        var isReady = false
            private set
    }
}

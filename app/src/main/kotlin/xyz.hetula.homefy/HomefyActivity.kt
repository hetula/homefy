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
 */

package xyz.hetula.homefy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
abstract class HomefyActivity : AppCompatActivity() {
    private var mKillReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "Creating HomefyActivity")
        super.onCreate(savedInstanceState)
        val filter = IntentFilter(KILL_INTENT)
        mKillReceiver = (object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "onReceive: " + intent?.action)
                if (intent?.action == KILL_INTENT) {
                    Log.d(TAG, "Finishing Activity!")
                    finishAndRemoveTask()
                }
            }
        })
        applicationContext.registerReceiver(mKillReceiver, filter)
    }

    override fun onDestroy() {
        Log.d(TAG, "Destroying HomefyActivity")
        super.onDestroy()
        applicationContext.unregisterReceiver(mKillReceiver)
    }

    companion object {
        private val TAG = "HomefyActivity"
        val KILL_INTENT = "xyz.hetula.homefy.KILL_INTENT"
    }
}

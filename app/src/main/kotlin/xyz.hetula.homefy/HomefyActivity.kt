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

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.annotation.CallSuper
import android.support.v7.app.AppCompatActivity
import android.util.Log
import xyz.hetula.homefy.service.HomefyService

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
abstract class HomefyActivity : AppCompatActivity() {
    lateinit var homefy: HomefyService
    private val mHomefyConnection = HomefyConnection { serviceConnected(it) }
    private var mKillReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, "Creating HomefyActivity")
        super.onCreate(savedInstanceState)
        val filter = IntentFilter(KILL_INTENT)
        mKillReceiver = (object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.v(TAG, "onReceive: " + intent?.action)
                if (intent?.action == KILL_INTENT) {
                    Log.v(TAG, "Finishing Activity!")
                    finishAffinity()
                }
            }
        })
        applicationContext.registerReceiver(mKillReceiver, filter)
    }

    override fun onStart() {
        Log.v("HomefyActivity", "onStart(): " + javaClass.simpleName)
        super.onStart()
        val intent = Intent(this, HomefyService::class.java)
        if (!bindService(intent, mHomefyConnection,
                Context.BIND_AUTO_CREATE or Context.BIND_ABOVE_CLIENT)) {
            unbindService(mHomefyConnection)
        }
    }

    override fun onStop() {
        Log.v("HomefyActivity", "onStop(): " + javaClass.simpleName)
        super.onStop()
        unbindService(mHomefyConnection)
    }


    override fun onDestroy() {
        Log.v(TAG, "Destroying HomefyActivity")
        super.onDestroy()
        applicationContext.unregisterReceiver(mKillReceiver)
        mKillReceiver = null
    }

    @CallSuper
    protected open fun serviceConnected(service: HomefyService) {
        homefy = service
    }

    internal class HomefyConnection(private val serviceCallback: (HomefyService) -> Unit) :
            ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.v("HomefyConnection", "HomefyService Disconnected!")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.v("HomefyConnection", "HomefyService Connected!")
            if (service != null) {
                serviceCallback((service as HomefyService.HomefyBinder).getService())
            }
        }

    }

    companion object {
        private val TAG = "HomefyActivity"
        val KILL_INTENT = "xyz.hetula.homefy.KILL_INTENT"
    }
}

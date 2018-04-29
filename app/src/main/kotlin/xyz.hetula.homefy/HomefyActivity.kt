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
        private const val TAG = "HomefyActivity"
        const val KILL_INTENT = "xyz.hetula.homefy.KILL_INTENT"
    }
}

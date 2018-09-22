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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

open class EasyBroadcastReceiver(vararg actions: String, private val onAction: ((Intent) -> Unit)? = null) : BroadcastReceiver() {
    private val mIntentFilter = IntentFilter()
    private var mRegistered = false

    init {
        actions.forEach { mIntentFilter.addAction(it) }
    }

    fun register(context: Context) {
        if (!mRegistered) {
            BroadcastHelper.registerLocalReceiver(context, this, mIntentFilter)
            mRegistered = true
        }
    }

    fun unregister(context: Context) {
        if (mRegistered) {
            BroadcastHelper.unregisterLocalReceiver(context, this)
            mRegistered = false
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        if (mIntentFilter.matchAction(intent.action)) {
            onAction?.invoke(intent)
        }
    }

}

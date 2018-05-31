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

package xyz.hetula.homefy.library

import android.os.AsyncTask

class SearchTask<T>(private val searchables: MutableSet<T>,
                    private val filter: (T) -> Boolean,
                    private val onReady: (MutableSet<T>) -> Unit) : AsyncTask<Void, Void, Void?>() {

    override fun doInBackground(vararg params: Void?): Void? {
        val iter = searchables.iterator()
        var item: T
        while (iter.hasNext()) {
            item = iter.next()
            if (filter(item)) {
                iter.remove()
            }
            if(isCancelled) {
                break
            }
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        onReady(searchables)
    }
}

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

import androidx.recyclerview.widget.SortedList
import xyz.hetula.homefy.forEach
import java.util.*

internal interface BaseAdapter<T> {
    val mItems: SortedList<T>
    var mCurrentSearchTask: SearchTask<T>?
    var mLastSearch: String
    var onSongPlay: (() -> Unit)?

    fun searchFilter(item: T, search: String): Boolean

    fun getOriginalItems(): List<T>

    fun searchWith(search: String) {
        if (search == mLastSearch) {
            return
        }
        mCurrentSearchTask?.cancel(true)
        if (search.isEmpty()) {
            mItems.clear()
            mItems.addAll(getOriginalItems())
        } else {
            val searchSet = if (search.length < mLastSearch.length) {
                HashSet(getOriginalItems())
            } else {
                val set = HashSet<T>()
                mItems.forEach { set.add(it) }
                set
            }
            val currentSearchTask = SearchTask(searchSet, { searchFilter(it, search) }) {
                var item: T
                mItems.beginBatchedUpdates()
                for (i in mItems.size() - 1 downTo 0) {
                    item = mItems[i]
                    if (!it.contains(item)) {
                        mItems.removeItemAt(i)
                    } else {
                        it.remove(item)
                    }
                }
                mItems.endBatchedUpdates()

                mItems.addAll(it)
            }
            currentSearchTask.execute()
            mCurrentSearchTask = currentSearchTask
        }
        mLastSearch = search
    }

    fun playAll()
}

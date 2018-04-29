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

package xyz.hetula.homefy.player

import java.util.*

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class PlayRequest internal constructor(private val play: Song,
                                       private val playContext: ArrayList<Song>) {

    init {
        if (!this.playContext.isEmpty()) {
            organizeList()
        }
    }

    fun fill(next: MutableCollection<Song>) {
        next.addAll(playContext)
    }

    fun getAny(rnd: Random): Song {
        val n = rnd.nextInt(playContext.size + 1)
        if (n >= playContext.size) return play
        return playContext[n]
    }

    private fun organizeList() {
        val index = playContext.indexOf(play)
        if (index == -1 || index == 0) return
        val temp = ArrayList<Song>()
        var i = 0
        val iterator = playContext.iterator()
        while (iterator.hasNext()) {
            if (i > index) break
            temp.add(iterator.next())
            iterator.remove()
            i++
        }
        playContext.addAll(temp)
    }
}

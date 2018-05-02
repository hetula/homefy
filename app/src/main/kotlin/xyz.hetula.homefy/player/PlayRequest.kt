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
class PlayRequest internal constructor(internal val play: Song,
                                       private val playContext: ArrayList<Song>,
                                       private val rnd: Random?) {

    fun fill(next: MutableCollection<Song>) {
        next.addAll(playContext)
    }

    fun getAny(): Song {
        val n = rnd?.nextInt(playContext.size + 1) ?: return play
        if (n >= playContext.size) return play
        return playContext[n]
    }
}

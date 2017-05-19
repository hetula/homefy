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

package xyz.hetula.homefy.player

import java.util.*

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class PlayRequest internal constructor(val play: Song, val playContext: ArrayList<Song>) {

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

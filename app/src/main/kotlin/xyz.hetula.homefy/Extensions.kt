/*
 * MIT License
 *
 * Copyright (c) 2018 Tuomo Heino
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
package xyz.hetula.homefy

import java.security.MessageDigest
import java.util.*

fun Long.parseSeconds(): String {
    val min = this / 60
    return String.format(Locale.getDefault(), "%02d:%02d", min, this - min * 60)
}

fun String.toSongHash(): String {
    val digest = MessageDigest.getInstance("SHA-1")
    val bytes = this.toByteArray()
    val hashed = digest.digest(bytes)

    val sb = StringBuilder()
    for (aHashed in hashed) {
        if (0xff and aHashed.toInt() < 0x10) {
            sb.append(0).append(Integer.toHexString(0xFF and aHashed.toInt()))
        } else {
            sb.append(Integer.toHexString(0xFF and aHashed.toInt()))
        }
    }
    return sb.toString()
}

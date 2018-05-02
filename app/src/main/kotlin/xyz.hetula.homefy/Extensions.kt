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

import java.security.MessageDigest
import java.util.*

fun Long.parseSeconds(): String {
    val min = this / 60
    return String.format(Locale.getDefault(), "%02d:%02d", min, this - min * 60)
}

fun String.toSHA1Hash(): String {
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

inline fun Int.useFor(useFunc: (Int) -> Unit) {
    if (this < 1) return
    for (i in 0 until this) {
        useFunc(i)
    }
}

fun <T> ArrayList<T>.removeAtIfPresent(index: Int): T? = if (this.isEmpty()) {
    null
} else {
    this.removeAt(index)
}

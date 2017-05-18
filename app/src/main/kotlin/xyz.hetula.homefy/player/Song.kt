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
 *
 */

package xyz.hetula.homefy.player

class Song : Comparable<Song> {
    val id: String
    val track: Int
    val title: String
    val artist: String
    val album: String
    val genre: String
    val length: Long
    val bitrate: Int
    val samplerate: Int
    val channels: String
    val type: String

    constructor() {
        // Default for reflection
        id = ""
        track = -1
        title = ""
        artist = ""
        album = ""
        genre = ""
        length = 0
        bitrate = 0
        samplerate = 0
        channels = ""
        type = ""

    }

    /**
     * Mock Song Constructor
     * @param id id
     * *
     * @param title title
     * *
     * @param artist artist
     * *
     * @param album album
     * *
     * @param length length in seconds
     */
    constructor(id: String, title: String, artist: String, album: String, length: Long) {
        this.id = id
        this.title = title
        this.artist = artist
        this.album = album
        this.length = if (length <= 0) 1 else length
        this.track = -1
        this.genre = "Unknown"
        this.bitrate = 128
        this.samplerate = 44100
        this.channels = "Stereo"
        this.type = "MockPEG3 Layer 1"
    }

    override fun compareTo(other: Song): Int {
        var c = album.compareTo(other.album)
        if (c != 0) return c
        c = Integer.compare(track, other.track)
        if (c != 0) return c
        c = artist.compareTo(other.artist)
        if (c != 0) return c
        return title.compareTo(other.title)
    }
}

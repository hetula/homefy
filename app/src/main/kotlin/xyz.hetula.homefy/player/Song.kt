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

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import com.google.gson.annotations.Expose

/**
 * @author Tuomo Heino
 * @version 1.0
 * @since 1.0
 */
class Song : Comparable<Song> {
    @Expose
    val id: String

    @Expose
    val track: Int

    @Expose
    val title: String

    @Expose
    val artist: String

    @Expose
    val album: String

    @Expose
    val genre: String

    @Expose
    val length: Long

    @Expose
    val bitrate: Int

    @Expose
    val samplerate: Int

    @Expose
    val channels: String

    @Expose
    val type: String

    var albumArt: Bitmap? = null

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

    fun clearAlbumArt() {
        val albumArtTmp = albumArt ?: return
        Log.d("Song", "clearAlbumArt(): Cleaning AlbumArt!")
        albumArtTmp.recycle()
        albumArt = null
    }

    fun toMediaMetadata(addAlbumArt: Boolean = false): MediaMetadataCompat {
        val metadata = MediaMetadataCompat.Builder()
        if (track > 0) metadata.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, track.toLong())
        if (addAlbumArt && albumArt != null) {
            metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
        }
        return metadata
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, length * 1000)
                .build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Song

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    inline fun getMimeType(consumer: (String) -> Unit) {
        if (type.startsWith("MPEG-")) consumer("audio/mpeg")
        // mp4
        if (type.startsWith("AAC")) consumer("audio/mp4")
        // FLAC
        if (type.startsWith("FLAC")) consumer("audio/flac")
        // wma
        if (type.startsWith("ASF")) consumer("audio/x-ms-wma")
        // wav
        if (type.startsWith("WAV")) consumer("audio/wav")
        // ogg
        if (type.startsWith("Ogg")) consumer("audio/ogg")
    }

    inline fun getFileType(consumer: (String) -> Unit) {
        if (type.startsWith("MPEG-")) consumer("mp3")
        // mp4
        if (type.startsWith("AAC")) consumer("m4u")
        // FLAC
        if (type.startsWith("FLAC")) consumer("flac")
        // wma
        if (type.startsWith("ASF")) consumer("wma")
        // wav
        if (type.startsWith("WAV")) consumer("wav")
        // ogg
        if (type.startsWith("Ogg")) consumer("ogg")
    }

}

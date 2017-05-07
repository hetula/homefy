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

package xyz.hetula.homefy.player;

import android.support.annotation.NonNull;

public class Song implements Comparable<Song> {
    private String id;
    private int track;
    private String title;
    private String artist;
    private String album;
    private String genre;
    private long length;
    private int bitrate;
    private int samplerate;
    private String channels;
    private String type;

    public Song() {
        // Default for reflection
    }

    /**
     * Mock Song Constructor
     * @param id id
     * @param title title
     * @param artist artist
     * @param album album
     * @param length length in seconds
     */
    public Song(String id, String title, String artist, String album, long length) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.length = length <= 0 ? 1 : length;
        this.track = -1;
        this.genre = "Unknown";
        this.bitrate = 128;
        this.samplerate = 44100;
        this.channels = "Stereo";
        this.type = "MockPEG3 Layer 1";
    }

    public String getId() {
        return id;
    }

    public int getTrack() {
        return track;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getGenre() {
        return genre;
    }

    public long getLength() {
        return length;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getSamplerate() {
        return samplerate;
    }

    public String getChannels() {
        return channels;
    }

    public String getType() {
        return type;
    }

    @Override
    public int compareTo(@NonNull Song o) {
        int c = album.compareTo(o.album);
        if(c != 0) return c;
        c = Integer.compare(track, o.track);
        if(c != 0) return c;
        c = artist.compareTo(o.artist);
        if(c != 0) return c;
        return title.compareTo(o.title);
    }
}

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

package xyz.hetula.homefy.library;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.hetula.homefy.player.Song;
import xyz.hetula.homefy.service.Homefy;

/**
 * Library Class that implements storing all songs
 * from Server.
 * Caches all artists and albums and creates song lists
 * for each of them ready.
 * All of this is held in memory so using very big
 * Homefy Library will cause big memory usage.
 */
public class HomefyLibrary {
    private Map<String, Song> mSongDatabase;
    private Map<String, List<Song>> mArtistCache;
    private Map<String, List<Song>> mAlbumCache;
    private List<Song> mMusic;
    private List<String> mAlbums;
    private List<String> mArtists;

    public void initialize(List<Song> music) {
        Log.d("HomefyLibrary", "Initializing with " + music.size() + " songs!");
        long start = System.currentTimeMillis();
        mMusic = music;
        Collections.sort(mMusic);

        // Take account load factor, probably too low but can be optimized later
        // TODO Check correct load factor
        mSongDatabase = new HashMap<>((int)(music.size() * 1.1));
        mArtistCache = new HashMap<>();
        mAlbumCache = new HashMap<>();

        for (Song song : music) {
            mSongDatabase.put(song.getId(), song);
            createAndAdd(mArtistCache, song.getArtist(), song);
            createAndAdd(mAlbumCache, song.getAlbum(), song);
        }

        // Init lists

        mAlbums = new ArrayList<>(mAlbumCache.keySet());
        mArtists = new ArrayList<>(mArtistCache.keySet());

        // Sort
        Collections.sort(mAlbums);
        Collections.sort(mArtists);
        long time = System.currentTimeMillis() - start;
        Log.d("HomefyLibrary", "Library initialized in " + time + " ms");
    }

    public void release() {
        mSongDatabase.clear();
        mArtistCache.clear();
        mAlbumCache.clear();
        mMusic.clear();
        mAlbums.clear();
        mArtists.clear();

    }

    private void createAndAdd(Map<String, List<Song>> cache, String key, Song song) {
        List<Song> list = cache.get(key);
        if (list == null) {
            list = new ArrayList<>();
            cache.put(key, list);
        }
        list.add(song);
    }

    @NonNull
    public List<Song> getSongs() {
        return mMusic;
    }

    @NonNull
    public List<String> getArtists() {
        return mArtists;
    }

    @NonNull
    public List<String> getAlbums() {
        return mAlbums;
    }

    @NonNull
    public List<Song> getArtistSongs(@NonNull String artist) {
        return getFromCache(mArtistCache, artist);
    }

    @NonNull
    public List<Song> getAlbumSongs(@NonNull String album) {
        return getFromCache(mAlbumCache, album);
    }

    private List<Song> getFromCache(Map<String, List<Song>> cache, String key) {
        List<Song> songs = cache.get(key);
        return songs == null ? Collections.emptyList() : songs;
    }

    @Nullable
    public Song getSong(@NonNull String id) {
        return mSongDatabase.get(id);
    }

    @NonNull
    public String getPlayPath(@NonNull Song song) {
        return Homefy.protocol().getServer() + "/play/" + song.getId();
    }
}

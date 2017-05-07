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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.hetula.homefy.player.Song;
import xyz.hetula.homefy.service.Homefy;

public class HomefyLibrary {
    private Map<String, Song> mSongDatabase;
    private List<Song> mMusic;

    public void initialize(List<Song> music) {
        mSongDatabase = new HashMap<>(music.size());
        for(Song song : music) {
            mSongDatabase.put(song.getId(), song);
        }
        mMusic = new ArrayList<>(music);
        Collections.sort(mMusic);
    }

    @NonNull
    public List<Song> getSongs() {
        return mMusic;
    }

    @Nullable
    public Song getSong(@NonNull String id) {
        return mSongDatabase.get(id);
    }

    @NonNull
    public String getPlayPath(@NonNull Song song) {
        return Homefy.protocol().getServer() + "/play/"+song.getId();
    }
}

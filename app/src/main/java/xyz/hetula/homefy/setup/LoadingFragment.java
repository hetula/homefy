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

package xyz.hetula.homefy.setup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import xyz.hetula.homefy.MainFragment;
import xyz.hetula.homefy.R;
import xyz.hetula.homefy.player.Song;
import xyz.hetula.homefy.service.Homefy;

public class LoadingFragment extends Fragment {
    private AtomicInteger mCount;
    private List<Song> mSongs = new ArrayList<>();
    private TextView mLoaded;
    private int mSongsTotal;
    private long mLoadStarted;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FrameLayout main = (FrameLayout) inflater.inflate(R.layout.fragment_loading, container, false);
        mLoaded = (TextView) main.findViewById(R.id.txt_songs_loaded);
        mSongsTotal = Homefy.protocol().getInfo().getDatabaseSize();
        initialize();
        return main;
    }

    private void initialize() {
        mSongs.clear();
        mLoadStarted = System.currentTimeMillis();
        Homefy.protocol().requestPages(250,
                this::fetchData,
                volleyError -> Toast.makeText(getContext(),
                        "Error when Connecting!", Toast.LENGTH_LONG).show());

    }

    private void fetchData(String[] urls) {
        mCount = new AtomicInteger(urls.length);
        for (String url : urls) {
            Homefy.protocol().request(
                    url,
                    this::onSongs,
                    volleyError -> Toast.makeText(getContext(),
                            "Error when Connecting!", Toast.LENGTH_LONG).show(),
                    Song[].class);
        }
    }

    private synchronized boolean ready() {
        return mCount.decrementAndGet() == 0;
    }

    private void onSongs(Song[] songs) {
        mSongs.addAll(Arrays.asList(songs));
        mLoaded.setText(getContext().getString(R.string.songs_loaded, mSongs.size(), mSongsTotal));
        if(ready()) {
            long time = System.currentTimeMillis() - mLoadStarted;
            Log.d("LoadingFragment", "Songs loaded in " + time + " ms");

            initializeHomefy();
        }
    }

    private void initializeHomefy() {
        List<Song> songs = mSongs;
        mSongs = new ArrayList<>(); // Create new list so old one can't be used here
        // initialize will use given list and does not create new one.
        Homefy.library().initialize(songs);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new MainFragment())
                .commit();
    }
}

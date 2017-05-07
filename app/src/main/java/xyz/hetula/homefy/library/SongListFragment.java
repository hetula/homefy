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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import xyz.hetula.homefy.R;
import xyz.hetula.homefy.service.Homefy;

public class SongListFragment extends Fragment {
    public static final String LIST_TYPE_KEY = "SongListFragment_LIST_TYPE_KEY";
    public static final String LIST_NAME_KEY = "SongListFragment_LIST_NAME_KEY";

    public static final int ALL_MUSIC = 1;
    public static final int ARTISTS = 2;
    public static final int ALBUMS = 3;
    public static final int ARTIST_MUSIC = 4;
    public static final int ALBUM_MUSIC = 5;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_song_list, container, false);
        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));

        Bundle args = getArguments();
        int type = args.getInt(LIST_TYPE_KEY);
        String name = args.getString(LIST_NAME_KEY, "ERRORROROR");

        RecyclerView.Adapter<?> adapter;
        switch (type) {
            case ALL_MUSIC:
                adapter = new SongAdapter(Homefy.library().getSongs());
                break;
            case ARTISTS:
                adapter = new SongListAdapter(Homefy.library().getArtists(),
                        artist -> Homefy.library().getArtistSongs(artist).size(),
                        this::onArtistClick);
                break;
            case ALBUMS:
                adapter = new SongListAdapter(Homefy.library().getAlbums(),
                        album -> Homefy.library().getAlbumSongs(album).size(),
                        this::onAlbumClick);
                break;
            case ARTIST_MUSIC:
                adapter = new SongAdapter(Homefy.library().getArtistSongs(name));
                break;
            case ALBUM_MUSIC:
                adapter = new SongAdapter(Homefy.library().getAlbumSongs(name));
                break;
            default:
                adapter = null;
                Log.e("SongListFragment", "Invalid TYPE: " + type);
                break;

        }
        if (adapter != null) {
            recyclerView.setAdapter(adapter);
        }
        return root;
    }

    private void onArtistClick(String artist) {
        Bundle args = new Bundle();
        args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ARTIST_MUSIC);
        args.putString(SongListFragment.LIST_NAME_KEY, artist);
        createSongListFragment(args);
    }

    private void onAlbumClick(String album) {
        Bundle args = new Bundle();
        args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ALBUM_MUSIC);
        args.putString(SongListFragment.LIST_NAME_KEY, album);
        createSongListFragment(args);
    }

    private void createSongListFragment(@NonNull Bundle args) {
        SongListFragment fragment = new SongListFragment();
        fragment.setArguments(args);
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .add(R.id.container, fragment)
                .show(fragment)
                .commit();
    }
}

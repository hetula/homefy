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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import xyz.hetula.homefy.R;

public class LibraryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FrameLayout main = (FrameLayout) inflater.inflate(R.layout.fragment_library, container, false);
        View libraryMusic = main.findViewById(R.id.library_music);
        View libraryArtists = main.findViewById(R.id.library_artists);
        View libraryAlbums = main.findViewById(R.id.library_albums);

        View.OnClickListener clicks = this::onLibraryClick;
        libraryMusic.setOnClickListener(clicks);
        libraryArtists.setOnClickListener(clicks);
        libraryAlbums.setOnClickListener(clicks);
        return main;
    }

    private void onLibraryClick(View v) {
        Bundle args = new Bundle();
        switch (v.getId()) {
            case R.id.library_music:
                args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ALL_MUSIC);
                break;
            case R.id.library_albums:
                args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ALBUMS);
                break;
            case R.id.library_artists:
                args.putInt(SongListFragment.LIST_TYPE_KEY, SongListFragment.ARTISTS);
                break;
            default:
                Log.w("LibraryFragment", "Unhandled Click from " + v);
                return;
        }
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

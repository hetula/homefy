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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import xyz.hetula.homefy.R;
import xyz.hetula.homefy.service.Homefy;

public class PlayerFragment extends Fragment {
    private PlaybackListener mPlaybackListener = (song, state) -> updateSongInfo();
    private TextView mTxtTitle;
    private TextView mTxtArtist;
    private TextView mTxtAlbum;
    private ImageButton mBtnPausePlay;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LinearLayout main = (LinearLayout) inflater.inflate(R.layout.fragment_player, container, false);
        mTxtTitle = (TextView) main.findViewById(R.id.txt_song_title);
        mTxtArtist = (TextView) main.findViewById(R.id.txt_song_artist);
        mTxtAlbum = (TextView) main.findViewById(R.id.txt_song_album);
        mBtnPausePlay = (ImageButton) main.findViewById(R.id.btn_play_pause);
        ImageButton btnPlayback = (ImageButton) main.findViewById(R.id.btn_playback);
        View btnStop = main.findViewById(R.id.btn_stop);
        View btnNext = main.findViewById(R.id.btn_next);
        View btnPrevious = main.findViewById(R.id.btn_previous);

        btnStop.setOnClickListener(v -> Homefy.player().stop());
        btnNext.setOnClickListener(v -> {
            Homefy.player().next();
            updateSongInfo();
        });
        mBtnPausePlay.setOnClickListener(v -> Homefy.player().pauseResume());

        return main;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSongInfo();
        Homefy.player().registerPlaybackListener(mPlaybackListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        Homefy.player().unregisterPlaybackListener(mPlaybackListener);
    }

    private void updateSongInfo() {
        Song now = Homefy.player().nowPlaying();
        if(now != null) {
            mTxtTitle.setText(String.format(Locale.getDefault(),
                    "%d - %s", now.getTrack(), now.getTitle()));
            mTxtArtist.setText(now.getArtist());
            mTxtAlbum.setText(now.getAlbum());
            if(Homefy.player().isPaused()) {
                mBtnPausePlay.setImageResource(R.drawable.ic_play_circle);
            } else if(Homefy.player().isPlaying()) {
                mBtnPausePlay.setImageResource(R.drawable.ic_pause_circle);
            }
        } else {
            mTxtTitle.setText(null);
            mTxtArtist.setText(null);
            mTxtAlbum.setText(null);
            mBtnPausePlay.setImageResource(R.drawable.ic_play_circle);
        }

    }
}

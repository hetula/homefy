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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import xyz.hetula.homefy.R;
import xyz.hetula.homefy.Utils;
import xyz.hetula.homefy.player.Song;
import xyz.hetula.homefy.service.Homefy;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> mSongs;

    public SongAdapter(List<Song> songs) {
        this.mSongs = new ArrayList<>(songs);
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View songView = inflater.inflate(R.layout.view_song, parent, false);
        return new SongViewHolder(songView);
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        Song song = mSongs.get(position);
        if(song.getTrack() < 0) {
            holder.txtTrackTitle.setText(song.getTitle());
        } else {
            holder.txtTrackTitle.setText(String.format(Locale.getDefault(),"%d - %s", song.getTrack(), song.getTitle()));
        }
        holder.txtArtistAlbum.setText(String.format(Locale.getDefault(), "%s - %s", song.getArtist(), song.getAlbum()));
        holder.txtLength.setText(Utils.parseSeconds(song.getLength()));
        holder.itemView.setOnClickListener(v -> Homefy.player().play(song, mSongs));
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        final TextView txtTrackTitle;
        final TextView txtArtistAlbum;
        final TextView txtLength;

        SongViewHolder(View itemView) {
            super(itemView);
            txtTrackTitle = (TextView) itemView.findViewById(R.id.song_track_title);
            txtArtistAlbum = (TextView) itemView.findViewById(R.id.song_artist_album);
            txtLength = (TextView) itemView.findViewById(R.id.song_length);
        }
    }
}

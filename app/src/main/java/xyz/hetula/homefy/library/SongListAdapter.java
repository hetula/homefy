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
 */

package xyz.hetula.homefy.library;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xyz.hetula.functional.Consumer;
import xyz.hetula.functional.Function;
import xyz.hetula.homefy.R;

/**
 * Created by tuomo on 7.5.2017.
 */

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongListViewHolder> {
    private List<String> mNameList;
    private Function<String, Integer> mCountFetch;
    private Consumer<String> mClick;

    public SongListAdapter(List<String> names, Function<String, Integer> countFetch, Consumer<String> click) {
        this.mNameList = new ArrayList<>(names);
        this.mClick = click;
        this.mCountFetch = countFetch;
    }

    @Override
    public SongListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View songView = inflater.inflate(R.layout.view_songlist, parent, false);
        return new SongListViewHolder(songView);
    }

    @Override
    public void onBindViewHolder(SongListViewHolder holder, int position) {
        String info = mNameList.get(position);
        holder.txtMainInfo.setText(info);
        int count = mCountFetch.apply(info);
        Context ctx = holder.itemView.getContext();
        String str = ctx.getResources().getQuantityString(R.plurals.song_count, count, count);
        holder.txtMoreInfo.setText(str);
        holder.itemView.setOnClickListener(v -> mClick.accept(info));
    }

    @Override
    public int getItemCount() {
        return mNameList.size();
    }

    static class SongListViewHolder extends RecyclerView.ViewHolder {
        final TextView txtMainInfo;
        final TextView txtMoreInfo;

        SongListViewHolder(View itemView) {
            super(itemView);
            txtMainInfo = (TextView) itemView.findViewById(R.id.txt_main_info);
            txtMoreInfo = (TextView) itemView.findViewById(R.id.txt_more_info);
        }
    }
}

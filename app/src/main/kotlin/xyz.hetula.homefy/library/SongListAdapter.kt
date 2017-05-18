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

package xyz.hetula.homefy.library

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.ArrayList

import xyz.hetula.homefy.R

/**
 * @author Tuomo Heino
 */
internal class SongListAdapter(names: List<String>,
                               private val mCountFetch: (String) -> Int,
                               private val mClick: (String) -> Unit) :
        RecyclerView.Adapter<SongListAdapter.SongListViewHolder>() {

    private val mNameList: List<String>

    init {
        this.mNameList = ArrayList(names)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val songView = inflater.inflate(R.layout.view_songlist, parent, false)
        return SongListViewHolder(songView)
    }

    override fun onBindViewHolder(holder: SongListViewHolder, position: Int) {
        val info = mNameList[position]
        holder.txtMainInfo.text = info
        val count = mCountFetch(info)
        val ctx = holder.itemView.context
        val str = ctx.resources.getQuantityString(R.plurals.song_count, count, count)
        holder.txtMoreInfo.text = str
        holder.itemView.setOnClickListener { _ -> mClick(info) }
    }

    override fun getItemCount(): Int {
        return mNameList.size
    }

    internal class SongListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtMainInfo: TextView = itemView.findViewById(R.id.txt_main_info) as TextView
        val txtMoreInfo: TextView = itemView.findViewById(R.id.txt_more_info) as TextView
    }
}

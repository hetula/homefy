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

package xyz.hetula.homefy.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.List;

import xyz.hetula.functional.Consumer;
import xyz.hetula.homefy.player.Song;
import xyz.hetula.homefy.service.protocol.VersionInfo;

public class DefaultHomefyProtocol implements HomefyProtocol {
    private static final String TAG = "HomefyProtocol";
    private RequestQueue mQueryQueue;
    private String mServerAddress;

    public DefaultHomefyProtocol(Context context) {
        mQueryQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    @Override
    public void setServer(String address) {
        this.mServerAddress = address;
    }

    @Override
    public String getServer() {
        return mServerAddress;
    }

    @Override
    public void requestVersionInfo(Consumer<VersionInfo> versionConsumer) {
        Request<String> testReq = new StringRequest(Request.Method.GET, mServerAddress+"/version",
                str -> versionConsumer.accept(new VersionInfo()),
                error -> Log.e(TAG, error.toString()));
        mQueryQueue.add(testReq);
    }

    @Override
    public void requestSongs(Consumer<List<Song>> songsConsumer) {
        // TODO Songs Requests and parsing from json!
    }

    @Override
    public void requestSong(String id, Consumer<Song> songConsumer) {
        // TODO Song request and parsing from json!
    }

    @Override
    public void release() {
        mQueryQueue.stop();
        mQueryQueue = null;
    }
}

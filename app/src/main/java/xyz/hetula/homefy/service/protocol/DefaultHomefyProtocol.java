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

package xyz.hetula.homefy.service.protocol;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import xyz.hetula.functional.Consumer;
import xyz.hetula.homefy.player.Song;

public class DefaultHomefyProtocol implements HomefyProtocol {
    private static final String TAG = "HomefyProtocol";
    private RequestQueue mQueryQueue;
    private String mServerAddress;
    private String mUserPass;

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
    public void setAuth(String user, String pass) {
        mUserPass = new String(Base64.encode((user+":"+pass)
                .getBytes(StandardCharsets.UTF_8), 0), StandardCharsets.UTF_8);
    }

    @Override
    public void requestVersionInfo(Consumer<VersionInfo> versionConsumer,
                                   Consumer<VolleyError> errorConsumer) {
        GsonRequest<VersionInfo> versionReq = new GsonRequest<>(
                mServerAddress + "/version",
                VersionInfo.class,
                versionConsumer::accept,
                error -> {
                    Log.e(TAG, error.toString());
                    if (errorConsumer != null) {
                        errorConsumer.accept(error);
                    }
                });
        mQueryQueue.add(versionReq);
    }

    @Override
    public void requestVersionInfoAuth(Consumer<VersionInfo> versionConsumer, Consumer<VolleyError> errorConsumer) {
        GsonRequest<VersionInfo> versionReq = new GsonRequest<>(
                mServerAddress + "/version/auth",
                VersionInfo.class,
                versionConsumer::accept,
                error -> {
                    Log.e(TAG, error.toString());
                    if (errorConsumer != null) {
                        errorConsumer.accept(error);
                    }
                });
        appendHeaders(versionReq);
        mQueryQueue.add(versionReq);
    }

    @Override
    public void requestSongs(Consumer<Song[]> songsConsumer,
                             Consumer<VolleyError> errorConsumer) {
        GsonRequest<Song[]> songsReq = new GsonRequest<>(
                mServerAddress + "/songs",
                Song[].class,
                songsConsumer::accept,
                error -> {
                    Log.e(TAG, error.toString());
                    if (errorConsumer != null) {
                        errorConsumer.accept(error);
                    }
                });
        appendHeaders(songsReq);
        mQueryQueue.add(songsReq);
    }

    @Override
    public void requestSong(String id, Consumer<Song> songConsumer,
                            Consumer<VolleyError> errorConsumer) {
        GsonRequest<Song> songReq = new GsonRequest<>(
                mServerAddress + "/song/" + id,
                Song.class,
                songConsumer::accept,
                error -> {
                    Log.e(TAG, error.toString());
                    if (errorConsumer != null) {
                        errorConsumer.accept(error);
                    }
                }
        );
        appendHeaders(songReq);
        mQueryQueue.add(songReq);
    }

    @Override
    public void release() {
        mQueryQueue.stop();
        mQueryQueue = null;
    }

    @Override
    public void addAuthHeader(Map<String, String> headers) {
        if(TextUtils.isEmpty(mUserPass)) return;
        headers.put("Authorization", "Basic " +  mUserPass);
    }

    private void appendHeaders(GsonRequest<?> request) {
        if(TextUtils.isEmpty(mUserPass)) return;
        request.putHeader("Authorization", "Basic " +  mUserPass);
    }
}

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

import com.android.volley.VolleyError;

import java.util.Map;

import xyz.hetula.functional.Consumer;
import xyz.hetula.homefy.player.Song;

public interface HomefyProtocol {
    void setServer(String address);

    String getServer();

    void setAuth(String user, String pass);

    void addAuthHeader(Map<String, String> headers);

    void requestVersionInfo(Consumer<VersionInfo> versionConsumer, Consumer<VolleyError> errorConsumer);

    void requestVersionInfoAuth(Consumer<VersionInfo> versionConsumer, Consumer<VolleyError> errorConsumer);

    void requestSongs(Consumer<Song[]> songsConsumer, Consumer<VolleyError> errorConsumer);

    void requestSong(String id, Consumer<Song> songConsumer, Consumer<VolleyError> errorConsumer);

    void release();
}

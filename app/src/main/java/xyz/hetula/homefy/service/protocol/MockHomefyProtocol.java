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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import xyz.hetula.functional.Consumer;
import xyz.hetula.homefy.player.Song;

public class MockHomefyProtocol implements HomefyProtocol {
    private List<Song> songs;

    public MockHomefyProtocol() {
        songs = new ArrayList<>();
        Random rnd = new Random(2);
        for(int artist = 1; artist <= 25; artist++) {
            for(int album = 1; album <= 10; album++) {
                for(int title = 1; title <= 13; title++) {
                    String id = Integer.toHexString(artist) +
                            Integer.toHexString(album) +
                            Integer.toHexString(title) +
                            Integer.toHexString(rnd.nextInt(Integer.MAX_VALUE - 20));

                    songs.add(new Song(id,
                            "Title " + title,
                            "Artist " + artist,
                            "Album " + album,
                            30 + rnd.nextInt(180)));
                }
            }
        }
    }

    @Override
    public void setServer(String address) {
        // Not used
    }

    @Override
    public String getServer() {
        return "MOCK";
    }

    @Override
    public void requestVersionInfo(Consumer<VersionInfo> versionConsumer,
                                   Consumer<VolleyError> errorConsumer) {
        versionConsumer.accept(new VersionInfo(
                "Homefy",
                "1.0_Mock",
                VersionInfo.AuthType.NONE));
    }

    @Override
    public void requestSongs(Consumer<Song[]> songsConsumer,
                             Consumer<VolleyError> errorConsumer) {
        songsConsumer.accept(songs.toArray(new Song[0]));
    }

    @Override
    public void requestSong(String id, Consumer<Song> songConsumer,
                            Consumer<VolleyError> errorConsumer) {
        for(Song s : songs) {
            if(s.getId().equals(id)) {
                songConsumer.accept(s);
            }
        }
    }

    @Override
    public void release() {
        songs.clear();
        songs = null;
    }
}

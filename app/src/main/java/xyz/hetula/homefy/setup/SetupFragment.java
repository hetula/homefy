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

package xyz.hetula.homefy.setup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import xyz.hetula.homefy.R;
import xyz.hetula.homefy.service.Homefy;
import xyz.hetula.homefy.service.protocol.VersionInfo;

public class SetupFragment extends Fragment {
    private EditText mAddress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LinearLayout main = (LinearLayout) inflater.inflate(R.layout.fragment_setup, container, false);
        Button connect = (Button) main.findViewById(R.id.btn_connect);
        connect.setOnClickListener(this::onConnect);
        mAddress = (EditText) main.findViewById(R.id.txt_address);
        return main;
    }

    private void onConnect(View v) {
        String address = mAddress.getText().toString();
        if (address.isEmpty()) return;
        Homefy.protocol().setServer(address);
        Homefy.protocol().requestVersionInfo(this::onVersionInfo);
    }

    private void onVersionInfo(VersionInfo versionInfo) {
        Log.d("SetupFragment", versionInfo.toString());
        if (versionInfo.getAuthentication() == VersionInfo.AuthType.NONE) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new LoadingFragment())
                    .commit();
        }
        // TODO Initialize possible authentication view here!
    }
}

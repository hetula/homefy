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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import xyz.hetula.homefy.R;
import xyz.hetula.homefy.service.Homefy;
import xyz.hetula.homefy.service.protocol.VersionInfo;

public class SetupFragment extends Fragment {
    private static final String ADDRESS_KEY = "SetupFragement.ADDRESS_KEY";
    private static final String USERNAME_KEY = "SetupFragement.USERNAME_KEY";
    private static final String PASSWORD_KEY = "SetupFragement.PASSWORD_KEY";

    private View mViewCredentials;
    private Button mConnect;
    private EditText mAddress;
    private EditText mUser;
    private EditText mPass;
    private boolean mConnecting;
    private boolean mNeedsAuth = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        LinearLayout main = (LinearLayout) inflater.inflate(R.layout.fragment_setup, container, false);
        mConnect = (Button) main.findViewById(R.id.btn_connect);
        mConnect.setOnClickListener(this::onConClick);
        mAddress = (EditText) main.findViewById(R.id.txt_address);
        mUser = (EditText) main.findViewById(R.id.txt_username);
        mPass = (EditText) main.findViewById(R.id.txt_password);
        mViewCredentials = main.findViewById(R.id.view_credentials);
        mAddress.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                onConnect();
            }
            return false;
        });
        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        mAddress.setText(pref.getString(ADDRESS_KEY, ""));
        mUser.setText(pref.getString(USERNAME_KEY, ""));
        mPass.setText(pref.getString(PASSWORD_KEY, ""));

        mConnecting = false;
        return main;
    }

    private void onConClick(View v) {
        if (mConnecting) return;
        String address = mAddress.getText().toString();
        Homefy.protocol().setServer(address);
        // Save it for later
        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        pref.edit().putString(ADDRESS_KEY, address).apply();

        if (address.isEmpty()) {
            Toast.makeText(getContext(), "Enter Server address!", Toast.LENGTH_LONG).show();
            return;
        }
        if (!mNeedsAuth) {
            onConnect();
        } else {
            String user = mUser.getText().toString();
            String pass = mPass.getText().toString();
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(getContext(), "Check Credentials!", Toast.LENGTH_LONG).show();
                return;
            }
            pref.edit()
                    .putString(USERNAME_KEY, user)
                    .putString(PASSWORD_KEY, pass)
                    .apply();
            onAuth(user, pass);
        }
    }

    private void onConnect() {
        mConnecting = true;
        Homefy.protocol().requestVersionInfo(this::onVersionInfo,
                volleyError -> {
                    Toast.makeText(getContext(),
                            "Error when Connecting!", Toast.LENGTH_LONG).show();
                    mConnecting = false;
                });
    }

    private void onAuth(String user, String pass) {
        mConnecting = true;
        Homefy.protocol().setAuth(user, pass);
        Homefy.protocol().requestVersionInfoAuth(this::onVersionInfoAuth,
                volleyError -> {
                    Toast.makeText(getContext(),
                            "Error when Connecting!", Toast.LENGTH_LONG).show();
                    mConnecting = false;
                });
    }

    private void onVersionInfo(VersionInfo versionInfo) {
        Log.d("SetupFragment", versionInfo.toString());
        mConnecting = false;
        switch (versionInfo.getAuthentication()) {
            case NONE:
                startLoading();
                break;
            case BASIC:
                mNeedsAuth = true;
                mConnect.setText(R.string.authenticate);
                mViewCredentials.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Enter Credentials!", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void onVersionInfoAuth(VersionInfo versionInfo) {
        Log.d("SetupFragment", versionInfo.toString());
        startLoading();
    }

    private void startLoading() {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LoadingFragment())
                .commit();
    }
}

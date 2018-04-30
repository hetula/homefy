/*
 * Copyright (c) 2018 Tuomo Heino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.hetula.homefy.setup

import android.content.Context
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import xyz.hetula.homefy.HomefyFragment
import xyz.hetula.homefy.R
import xyz.hetula.homefy.service.protocol.RequestError
import xyz.hetula.homefy.service.protocol.VersionInfo


class SetupFragment : HomefyFragment() {
    private lateinit var mMain: LinearLayout
    private lateinit var mViewCredentials: View
    private lateinit var mConnect: Button
    private lateinit var mAddress: TextInputEditText
    private lateinit var mUser: TextInputEditText
    private lateinit var mPass: TextInputEditText
    private var mConnecting = false
    private var mNeedsAuth = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val main = inflater.inflate(R.layout.fragment_setup, container, false) as LinearLayout
        mMain = main
        mConnect = main.findViewById(R.id.btn_connect)
        mAddress = main.findViewById(R.id.txt_address)
        mUser = main.findViewById(R.id.txt_username)
        mPass = main.findViewById(R.id.txt_password)
        mViewCredentials = main.findViewById(R.id.view_credentials)

        mConnect.setOnClickListener { _ -> onConnectClick() }
        mAddress.setOnEditorActionListener { _, _, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                onConnect()
            }
            false
        }
        val pref = activity!!.getPreferences(Context.MODE_PRIVATE)
        mAddress.setText(pref.getString(ADDRESS_KEY, ""))
        mUser.setText(pref.getString(USERNAME_KEY, ""))
        mPass.setText(pref.getString(PASSWORD_KEY, ""))

        mConnecting = false
        return main
    }

    private fun onConnectClick() {
        if (mConnecting) return
        val address = mAddress.text.toString()
        homefy().getProtocol().server = address

        val pref = activity!!.getPreferences(Context.MODE_PRIVATE)
        pref.edit().putString(ADDRESS_KEY, address).apply()

        if (address.isEmpty()) {
            Snackbar.make(mMain, R.string.setup_enter_server_address, Snackbar.LENGTH_SHORT).show()
            return
        }
        if (!mNeedsAuth) {
            onConnect()
        } else {
            val user = mUser.text.toString()
            val pass = mPass.text.toString()
            if (user.isEmpty()) {
                Snackbar.make(mMain, R.string.setup_check_credentials_username, Snackbar.LENGTH_SHORT).show()
                return
            }
            if(pass.isEmpty()) {
                Snackbar.make(mMain, R.string.setup_check_credentials_password, Snackbar.LENGTH_SHORT).show()
                return
            }
            pref.edit()
                    .putString(USERNAME_KEY, user)
                    .putString(PASSWORD_KEY, pass)
                    .apply()
            onAuth(user, pass)
        }
    }

    private fun onConnect() {
        mConnecting = true
        homefy().getProtocol().requestVersionInfo(this::onVersionInfo,
                { err ->
                    Snackbar.make(mMain, getRequestErrorStringRes(err), Snackbar.LENGTH_SHORT).show()
                    mConnecting = false
                })
    }

    private fun onAuth(user: String, pass: String) {
        mConnecting = true
        homefy().getProtocol().setAuth(user, pass)
        homefy().getProtocol().requestVersionInfoAuth(this::onVersionInfoAuth,
                { err ->
                    Snackbar.make(mMain, getRequestErrorStringRes(err), Snackbar.LENGTH_SHORT).show()
                    mConnecting = false
                })
    }

    private fun onVersionInfo(versionInfo: VersionInfo) {
        Log.d("SetupFragment", versionInfo.toString())
        mConnecting = false
        when (versionInfo.authentication) {
            VersionInfo.AuthType.NONE -> startLoading()
            VersionInfo.AuthType.BASIC -> {
                mNeedsAuth = true
                mConnect.setText(R.string.setup_authenticate)
                mViewCredentials.visibility = View.VISIBLE
            }
            else -> {
                Log.w(TAG, "Unsupported auth method")
                Snackbar.make(mMain, R.string.setup_unsupported_authentication, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun onVersionInfoAuth(versionInfo: VersionInfo) {
        Log.d("SetupFragment", versionInfo.toString())
        mConnecting = false
        startLoading()
    }

    private fun startLoading() {
        fragmentManager!!
                .beginTransaction()
                .replace(R.id.container, LoadingFragment())
                .commit()
    }

    @StringRes
    private fun getRequestErrorStringRes(error: RequestError): Int = when(error.errCode) {
        401 -> R.string.setup_authentication_error
        -1 -> R.string.setup_malformed_url
        else -> R.string.setup_connection_error
    }

    companion object {
        private const val TAG = "SetupFragment"
        private const val ADDRESS_KEY = "SetupFragement.ADDRESS_KEY"
        private const val USERNAME_KEY = "SetupFragement.USERNAME_KEY"
        private const val PASSWORD_KEY = "SetupFragement.PASSWORD_KEY"
    }
}

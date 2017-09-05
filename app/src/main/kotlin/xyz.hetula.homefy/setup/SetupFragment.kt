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

package xyz.hetula.homefy.setup

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

import xyz.hetula.homefy.R
import xyz.hetula.homefy.service.Homefy
import xyz.hetula.homefy.service.protocol.VersionInfo


class SetupFragment : Fragment() {
    private val TAG = "SetupFragment"

    private var mViewCredentials: View? = null
    private var mConnect: Button? = null
    private var mAddress: EditText? = null
    private var mUser: EditText? = null
    private var mPass: EditText? = null
    private var mConnecting: Boolean = false
    private var mNeedsAuth = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val main = inflater!!.inflate(R.layout.fragment_setup, container, false) as LinearLayout
        mConnect = main.findViewById(R.id.btn_connect)
        mConnect!!.setOnClickListener(this::onConClick)
        mAddress = main.findViewById(R.id.txt_address)
        mUser = main.findViewById(R.id.txt_username)
        mPass = main.findViewById(R.id.txt_password)
        mViewCredentials = main.findViewById(R.id.view_credentials)
        mAddress!!.setOnEditorActionListener { _, _, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                onConnect()
            }
            false
        }
        val pref = activity.getPreferences(Context.MODE_PRIVATE)
        mAddress!!.setText(pref.getString(ADDRESS_KEY, ""))
        mUser!!.setText(pref.getString(USERNAME_KEY, ""))
        mPass!!.setText(pref.getString(PASSWORD_KEY, ""))

        mConnecting = false
        return main
    }

    private fun onConClick(v: View) {
        if (mConnecting) return
        val address = mAddress!!.text.toString()
        Homefy.protocol().server = address
        // Save it for later
        val pref = activity.getPreferences(Context.MODE_PRIVATE)
        pref.edit().putString(ADDRESS_KEY, address).apply()

        if (address.isEmpty()) {
            Toast.makeText(context, "Enter Server address!", Toast.LENGTH_LONG).show()
            return
        }
        if (!mNeedsAuth) {
            onConnect()
        } else {
            val user = mUser!!.text.toString()
            val pass = mPass!!.text.toString()
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(context, "Check Credentials!", Toast.LENGTH_LONG).show()
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
        Homefy.protocol().requestVersionInfo(this::onVersionInfo,
                { _ ->
                    Toast.makeText(context,
                            "Error when Connecting!", Toast.LENGTH_LONG).show()
                    mConnecting = false
                })
    }

    private fun onAuth(user: String, pass: String) {
        mConnecting = true
        Homefy.protocol().setAuth(user, pass)
        Homefy.protocol().requestVersionInfoAuth(this::onVersionInfoAuth,
                { _ ->
                    Toast.makeText(context,
                            "Error when Connecting!", Toast.LENGTH_LONG).show()
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
                mConnect!!.setText(R.string.authenticate)
                mViewCredentials!!.visibility = View.VISIBLE
            }
            else -> {
                Log.w(TAG, "Unsupported auth method")
            }
        }
    }

    private fun onVersionInfoAuth(versionInfo: VersionInfo) {
        Log.d("SetupFragment", versionInfo.toString())
        startLoading()
    }

    private fun startLoading() {
        fragmentManager
                .beginTransaction()
                .replace(R.id.container, LoadingFragment())
                .commit()
    }

    companion object {
        private val ADDRESS_KEY = "SetupFragement.ADDRESS_KEY"
        private val USERNAME_KEY = "SetupFragement.USERNAME_KEY"
        private val PASSWORD_KEY = "SetupFragement.PASSWORD_KEY"
    }
}

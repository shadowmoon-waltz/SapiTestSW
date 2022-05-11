// Copyright 2022 shadowmoon_waltz
//
// This file is part of SapiTestSW.
//
// SapiTestSW is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
// SapiTestSW is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with SapiTestSW. If not, see <https://www.gnu.org/licenses/>.

package com.sw.sapitest.cli

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.sw.sapi.RECV") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
            val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: return
            val token = intent.getStringExtra("token") ?: return
            val prefs = context.getSharedPreferences("sapitest_cli", 0)
            val expected_token = prefs.getString("token", null) ?: return
            val srv_pkg = prefs.getString("srv_pkg", null) ?: return
            if (token == expected_token) {
                Toast.makeText(context, "SapiTestCliSW got valid RECV \"$text\" from \"$subject\" from \"$srv_pkg\"", Toast.LENGTH_LONG).show()
            }
        } else if (intent.action == "com.sw.sapi.UNINIT") {
            val token = intent.getStringExtra("token") ?: return
            val prefs = context.getSharedPreferences("sapitest_cli", 0)
            val expected_token = prefs.getString("token", null) ?: return
            val srv_pkg = prefs.getString("srv_pkg", null) ?: return
            if (token == expected_token) {
                prefs.edit().remove("token").remove("srv_pkg").commit()
                Toast.makeText(context, "SapiTestCliSW got valid UNINIT from \"$srv_pkg\"", Toast.LENGTH_LONG).show()
            }
        }
    }
}

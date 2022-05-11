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

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import android.view.WindowManager
import com.sw.sapitest.cli.databinding.MainLayoutBinding

class MainActivity: Activity() {
  private lateinit var binding: MainLayoutBinding
  private lateinit var prefs: SharedPreferences

  override protected fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    prefs = getSharedPreferences("sapitest_cli", 0)
    binding = MainLayoutBinding.inflate(getLayoutInflater())
    setContentView(binding.root)
    binding.btnSend.setOnClickListener { _ ->
      val token = prefs.getString("token", null)
      val srv_pkg = prefs.getString("srv_pkg", null)
      if (token != null && srv_pkg != null) {
        val msg = binding.etMessage.text.toString()
        val intent = Intent().apply {
          action = "com.sw.sapi.SEND"
          putExtra(Intent.EXTRA_TEXT, msg)
          putExtra("token", token)
          setPackage(srv_pkg)
        }
        sendBroadcast(intent)
        Toast.makeText(this@MainActivity, "Sent SEND to srv app ${srv_pkg}, ${msg}", Toast.LENGTH_LONG).show()
      }
    }
    if (savedInstanceState != null) {
      val message = savedInstanceState.getCharSequence("message", null)
      if (message != null) {
        binding.etMessage.setText(message)
      }
    }
  }

  override protected fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putCharSequence("message", binding.etMessage.text.toString())
  }

  override protected fun onResume() {
    super.onResume()
    if (!prefs.contains("token") || !prefs.contains("srv_pkg")) {
      binding.etMessage.setEnabled(false)
      binding.btnSend.setText("Not registered or token revoked")
      binding.btnSend.setEnabled(false)
    } else {
      binding.etMessage.setEnabled(true)
      binding.btnSend.setText("Send")
      binding.btnSend.setEnabled(true)
    }
  }
}

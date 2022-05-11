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
import android.os.Bundle
import android.widget.Toast
import com.sw.sapitest.cli.databinding.InitLayoutBinding

class InitActivity: Activity() {
  private lateinit var binding: InitLayoutBinding

  override protected fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val intent = getIntent()
    if (intent == null || intent.action != "com.sw.sapi.INIT") {
      finish()
      return
    }
    val token = intent.getStringExtra("token")
    val result_expected = intent.getIntExtra("result_expected", 0)
    val srv_pkg = intent.getStringExtra("srv_pkg")
    if (token == null || result_expected == 0 || srv_pkg == null) {
      finish()
      return
    }
    binding = InitLayoutBinding.inflate(getLayoutInflater())
    setContentView(binding.root)
    binding.btnDeny.setOnClickListener { _ ->
      setResult(-result_expected)
      finish()
    }
    binding.btnConfirm.setOnClickListener { _ ->
      getSharedPreferences("sapitest_cli", 0).edit().putString("token", token).putString("srv_pkg", srv_pkg).commit()
      setResult(result_expected)
      finish()
    }
  }
}

// Copyright 2022 shadowmoon_waltz
//
// This file is part of SapiTestSW.
//
// SapiTestSW is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
//
// SapiTestSW is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with SapiTestSW. If not, see <https://www.gnu.org/licenses/>.

package com.sw.sapitest.srv

import android.app.Activity
import android.content.ComponentName
import android.content.pm.PackageManager
import android.view.View
import android.widget.ArrayAdapter
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Toast
import com.sw.sapitest.srv.databinding.MainLayoutBinding
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class MainActivity: Activity() {
  companion object {
    private val INIT_FINISHED = 4200
    private val randStart = maxOf(1, Activity.RESULT_FIRST_USER)
    private val randRange = 1_000_000
  }

  private class AdapterItem(val app: String, val cls: String, val token: String?) {
    override fun toString() = if (app == "") "Select an app" else "[${if (token != null) '*' else ' '}] $app"
  }

  private lateinit var binding: MainLayoutBinding
  private lateinit var prefs: SharedPreferences
  private var expectedInitFinished: Int = 0

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.menu_refresh -> {
        refresh()
        true
      }
      R.id.menu_action -> {
        action()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun refresh() {
    val adapter = ArrayAdapter<AdapterItem>(this, android.R.layout.simple_spinner_item)
    val intent = Intent().apply {
      action = "com.sw.sapi.INIT"
      putExtra(Intent.EXTRA_TEXT, "Heyas!")
      type = "text/plain"
    }
    adapter.add(AdapterItem("", "", null))
    for (info in getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_ALL)) {
      if (info.activityInfo != null) {
        val token = prefs.getString("p ${info.activityInfo.packageName}", null)
        adapter.add(AdapterItem(info.activityInfo.packageName, info.activityInfo.name, token))
      }
    }
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    val oldSelection = binding.spinner.selectedItemPosition
    binding.spinner.adapter = adapter
    binding.spinner.setSelection(if (oldSelection >= 0) oldSelection else if (adapter.count > 0) 1 else 0)
  }

  override fun onPrepareOptionsMenu(menu: Menu): Boolean {
    val pos = binding.spinner.getSelectedItemPosition()
    if (pos > 0) {
      val item = binding.spinner.getItemAtPosition(pos) as? AdapterItem
      if (item != null) {
        val connected = (item.token != null)
        menu.findItem(R.id.menu_action)?.apply {
          title = if (connected) "Disconnect" else "Connect"
          setEnabled(true)
        }
        return true
      }
    }
    menu.findItem(R.id.menu_action)?.apply {
      title = "No app to (dis)connect"
      setEnabled(false)
    }
    return true
  }

  private fun handleSpin(pos: Int) {
    if (pos > 0) {
      val item = binding.spinner.getItemAtPosition(pos) as? AdapterItem
      if (item != null) {
        val connected = (item.token != null)
        binding.etMessage.setEnabled(connected)
        binding.btnMakeRecv.setEnabled(connected)
        return
      }
    }
    binding.etMessage.setEnabled(false)
    binding.btnMakeRecv.setEnabled(false)
  }

  private fun action() {
    val pos = binding.spinner.getSelectedItemPosition()
    if (pos > 0) {
      val item = binding.spinner.getItemAtPosition(pos) as? AdapterItem
      if (item != null) {
        if (item.token == null) {
          val token = UUID.randomUUID().toString()
          val t = ThreadLocalRandom.current().nextInt(randStart, randRange)
          expectedInitFinished = t
          prefs.edit().putString("i $t", "$token/${item.app}").commit()
          val intent = Intent().apply {
            action = "com.sw.sapi.INIT"
            putExtra(Intent.EXTRA_TEXT, "Heyas!")
            putExtra("token", token)
            putExtra("result_expected", t)
            putExtra("srv_pkg", getPackageName())
            type = "text/plain"
            component = ComponentName(item.app, item.cls)
            setPackage(item.app)
          }
          Toast.makeText(this@MainActivity, "Providing token to app", Toast.LENGTH_LONG).show()
          try {
            startActivityForResult(intent, INIT_FINISHED)
          } catch (_: Exception) {
            Toast.makeText(this@MainActivity, "Failed to start app", Toast.LENGTH_LONG).show()
          }
        } else {
          prefs.edit().remove("p ${item.app}").remove("t ${item.token}").commit()
          val intent = Intent().apply {
            action = "com.sw.sapi.UNINIT"
            putExtra("token", item.token)
            putExtra("srv_pkg", getPackageName())
            setPackage(item.app)
          }
          sendBroadcast(intent)
          Toast.makeText(this@MainActivity, "Revoked token for app ${item.app}", Toast.LENGTH_LONG).show()
          refresh()
        }
      }
    }
  }
  override protected fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    prefs = getSharedPreferences("sapitest_srv", 0)
    binding = MainLayoutBinding.inflate(getLayoutInflater())
    setContentView(binding.root)
    binding.spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        handleSpin(pos)
      }

      override fun onNothingSelected(parent: AdapterView<*>) {
      }
    })
    binding.btnMakeRecv.setOnClickListener { _ ->
      val pos = binding.spinner.getSelectedItemPosition()
      if (pos > 0) {
        val item = binding.spinner.getItemAtPosition(pos) as? AdapterItem
        if (item != null && item.token != null) {
          val intent = Intent().apply {
            action = "com.sw.sapi.RECV"
            putExtra(Intent.EXTRA_TEXT, binding.etMessage.text.toString())
            putExtra("token", item.token)
            putExtra("srv_pkg", getPackageName())
            putExtra(Intent.EXTRA_SUBJECT, "0")
            setPackage(item.app)
          }
          sendBroadcast(intent)
          Toast.makeText(this@MainActivity, "Sent RECV to app ${item.app}", Toast.LENGTH_LONG).show()
        }
      }
    }
    refresh()
    if (savedInstanceState != null) {
      val spinnerPos = savedInstanceState.getInt("spinnerPos", -1)
      if (spinnerPos >= 0 && spinnerPos < (binding.spinner.adapter?.count ?: 0)) {
        binding.spinner.setSelection(spinnerPos)
      }
      val message = savedInstanceState.getCharSequence("message", null)
      if (message != null) {
        binding.etMessage.setText(message)
      }
    }
  }

  override protected fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt("spinnerPos", binding.spinner.selectedItemPosition)
    outState.putCharSequence("message", binding.etMessage.text.toString())
  }

  override protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    val eif = expectedInitFinished
    if (eif == 0 || requestCode != INIT_FINISHED || (eif != resultCode && eif != -resultCode)) {
      return
    }
    val resultCode2 = Math.abs(resultCode)
    val s = prefs.getString("i ${resultCode2}", null)
    if (s == null || !s.contains('/')) {
      Toast.makeText(this, "SapiTestSrvSW init finished started with invalid result code", Toast.LENGTH_LONG).show()
      return
    }
    val (token, app) = s.split('/', limit=2)
    if (resultCode > 0) {
      Toast.makeText(this, "App indicated it accepted the token", Toast.LENGTH_LONG).show()
      prefs.edit().putString("p $app", token).putString("t $token", app).commit()
      refresh()
    } else {
      Toast.makeText(this, "App indicated it DID NOT accept the token", Toast.LENGTH_LONG).show()
      prefs.edit().remove("i ${resultCode2}").commit()
    }
    expectedInitFinished = 0
  }
}

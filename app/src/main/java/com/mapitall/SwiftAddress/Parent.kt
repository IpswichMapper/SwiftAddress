package com.mapitall.SwiftAddress

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceManager

class Parent : AppCompatActivity() {

    private val TAG = "Parent"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val intent: Intent

        Log.i(TAG, "In parent activity.")
        Log.i(TAG, "interface: ${sp.getString("interface", "Default")}")
        when(sp.getString("interface", "Default")) {
            "Default" -> {
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            "Classic" -> {
                intent = Intent(this, ClassicMainActivity::class.java)
                startActivity(intent)
            }
        }


        finish()
    }
}
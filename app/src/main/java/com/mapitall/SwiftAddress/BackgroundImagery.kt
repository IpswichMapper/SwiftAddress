package com.mapitall.SwiftAddress

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.preference.PreferenceManager
import com.google.android.material.card.MaterialCardView

class BackgroundImagery : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_background_imagery)

        val changeBackgroundImageryFragment = ChangeBackgroundImageryFragment()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, changeBackgroundImageryFragment)
            commit()
        }

    }




    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    fun onSavePressed(@Suppress("UNUSED_PARAMETER") view : View) {
        setResult(RESULT_OK)
        finish()
    }



    // Sends the chosen imagery back to the MainActivity.
    // Called by the two imagery buttons.
    // TODO : Make this interface clearer.
    fun sendImageryChosen(view: View) {
        val imageryButton = findViewById<Button>(view.id)
        val imageryButtonText = imageryButton.tag.toString()
        Log.i("imagery button Text", imageryButtonText)
        val intent = Intent(this, MainActivity::class.java)

        intent.putExtra("imagery_chosen", imageryButtonText)
        setResult(RESULT_OK, intent)
        finish()
    }
}
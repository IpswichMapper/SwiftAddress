package com.mapitall.SwiftAddress.BackgroundImagery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mapitall.SwiftAddress.MainActivity
import com.mapitall.SwiftAddress.R

class BackgroundImagery : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_background_imagery)

        val backgroundImageryFragment = BackgroundImageryFragment()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, backgroundImageryFragment)
            commit()
        }

    }

    // Sends the chosen imagery back to the MainActivity.
    // Called by the two imagery buttons.
    // TODO : Make this interface clearer.
    /*
    fun sendImageryChosen(view: View) {
        val imageryButton = findViewById<Button>(view.id)
        val imageryButtonText = imageryButton.tag.toString()
        Log.i("imagery button Text", imageryButtonText)
        val intent = Intent(this, MainActivity::class.java)

        intent.putExtra("imagery_chosen", imageryButtonText)
        setResult(RESULT_OK, intent)
        finish()
    }
    */

}
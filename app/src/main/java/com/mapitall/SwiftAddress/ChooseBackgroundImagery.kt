package com.mapitall.SwiftAddress

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView

class ChooseBackgroundImagery : AppCompatActivity() {

    val TAG = "ChooseBackgroundImagery"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_background_imagery)

        supportActionBar?.title = getString(R.string.choose_background_imagery)

    }

    // Sends the chosen imagery back to the mainactivity.
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

    fun changeImagery(view: View) {
        if (view is ImageView) {
            Log.i(TAG, "ImageView pressed.")

        } else if (view is Button) {

        }

    }
}
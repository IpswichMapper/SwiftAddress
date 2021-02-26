package com.mapitall.SwiftAddress

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.preference.PreferenceManager
import com.google.android.material.card.MaterialCardView

class ChooseBackgroundImagery : AppCompatActivity() {

    val TAG = "ChooseBackgroundImagery"
    private lateinit var imagery : String
    private lateinit var sp : SharedPreferences
    private lateinit var spEdit : SharedPreferences.Editor

    private lateinit var customImageryDialog : AlertDialog.Builder
    private lateinit var dialog : AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_background_imagery)

        customImageryDialog = AlertDialog.Builder(this)
        customImageryDialog.setTitle(getString(R.string.custom_imagery))
        val imageryEditText = EditText(this)
        customImageryDialog.setPositiveButton(getString(R.string.change_imagery)) { _, _ ->



            spEdit = sp.edit()
            spEdit.putString("imagery", "custom")
            spEdit.putString("custom-imagery", imageryEditText.text.toString())
            spEdit.apply()
            Toast.makeText(this, getString(R.string.unimplemented),
                Toast.LENGTH_SHORT).show()
        }
        customImageryDialog.setNeutralButton(getString(R.string.cancel)) { _, _ -> }

        val container = FrameLayout(this)
        val params : FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            MATCH_PARENT, WRAP_CONTENT
        )
        params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        imageryEditText.layoutParams = params
        imageryEditText.hint = getString(R.string.custom_slippy_url)
        container.addView(imageryEditText)


        customImageryDialog.setView(container)
        dialog = customImageryDialog.create()


        sp = PreferenceManager.getDefaultSharedPreferences(this)
        supportActionBar?.title = getString(R.string.choose_background_imagery)



        imagery = sp.getString("imagery", "osm-carto").toString()
        Log.i(TAG, imagery)
        when (imagery) {
            "osm-carto" -> {
                val cardView = findViewById<MaterialCardView>(R.id.osm_carto_card_view)
                cardView.strokeWidth = 5
                cardView.strokeColor = ContextCompat.getColor(this, R.color.border_blue)
            }
            "mapbox-satellite" -> {
                val cardView = findViewById<MaterialCardView>(R.id.mapbox_satellite_card_view)
                cardView.strokeWidth = 5
                cardView.strokeColor = ContextCompat.getColor(this, R.color.border_blue)
            }
            "bing-satellite" -> {
                val cardView = findViewById<MaterialCardView>(R.id.bing_satellite_card_view)
                cardView.strokeWidth = 5
                cardView.strokeColor = ContextCompat.getColor(this, R.color.border_blue)
            }
            "esri-satellite" -> {
                val cardView = findViewById<MaterialCardView>(R.id.esri_satellite_card_view)
                cardView.strokeWidth = 5
                cardView.strokeColor = ContextCompat.getColor(this, R.color.border_blue)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (dialog.isShowing) {
            dialog.dismiss()
        } else {
            setResult(RESULT_OK)
            finish()
        }
        return true
    }


    override fun onBackPressed() {
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

    fun changeImagery(view: View) {

        val linearLayout = findViewById<LinearLayout>(R.id.choose_background_imagery_linear_layout)

        for(child in linearLayout.children) {
            try {
                val cardView = child as MaterialCardView
                cardView.strokeWidth = 0
            } catch (e : Exception) {
                Log.w(TAG, e.message.toString())
            }
        }
        try {
            val frameLayout = findViewById<FrameLayout>(view.id)
            val cardView: MaterialCardView = frameLayout.parent as MaterialCardView
            cardView.strokeColor = ContextCompat.getColor(this, R.color.border_blue)
            cardView.strokeWidth = 5
        } catch (e : Exception) {
            Log.w(TAG, e.message.toString())
        }

        spEdit = sp.edit()
        when (view.id) {
            R.id.osm_carto_frame_view -> {
                if (imagery != "null") {
                    spEdit.putString("imagery", "osm-carto")
                }
                else {
                    spEdit.putString("imagery", "osm-carto")
                    Log.w(TAG, "imagery was null")
                }
                spEdit.apply()
            }
            R.id.mapbox_satellite_frame_layout -> {
                if (imagery != "null") {
                    spEdit.putString("imagery", "mapbox-satellite")
                }
                else {
                    spEdit.putString("imagery", "osm-carto")
                    Log.w(TAG, "imagery was null")
                }
                spEdit.apply()
            }
            R.id.bing_satellite_frame_layout -> {
                if (imagery != "null") {
                    spEdit.putString("imagery", "bing-satellite")
                } else {
                    spEdit.putString("imagery", "osm-carto")
                    Log.w(TAG, "imagery was null")
                }
                spEdit.apply()
            }
            R.id.custom_imagery -> {
                dialog.show()
            }
        }

    }
}
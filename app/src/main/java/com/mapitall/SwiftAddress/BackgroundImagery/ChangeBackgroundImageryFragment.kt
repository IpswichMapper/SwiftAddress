package com.mapitall.SwiftAddress.BackgroundImagery

import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.card.MaterialCardView
import com.mapitall.SwiftAddress.R


class ChangeBackgroundImageryFragment : Fragment(
        R.layout.fragment_change_background_imagery_fragement) {

    val TAG = "ChooseBackgroundImagery"
    private lateinit var imagery: String
    private lateinit var sp: SharedPreferences
    private lateinit var spEdit: SharedPreferences.Editor

    private lateinit var customImageryDialog: AlertDialog.Builder
    private lateinit var dialog: AlertDialog


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        customImageryDialog = AlertDialog.Builder(context)
        customImageryDialog.setTitle(getString(R.string.custom_imagery))
        val imageryEditText = EditText(context)

        sp = PreferenceManager.getDefaultSharedPreferences(context)
        var customImageryList = sp.getStringSet("custom_imagery_list", setOf())!!
                .toMutableList()

        val noCopyrightedMaterial = TextView(context)
        noCopyrightedMaterial.text = getString(R.string.no_copyrighted_material)
        noCopyrightedMaterial.setTypeface(null, Typeface.BOLD)
        noCopyrightedMaterial.setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.button_colors
            )
        )

        customImageryDialog.setPositiveButton(getString(R.string.change_imagery)) { _, _ ->

            customImageryList =
                    sp.getStringSet("custom_imagery_list", setOf())!!.toMutableList()
            spEdit = sp.edit()
            val imageryText = imageryEditText.text.toString()
            if (imageryText != "") {
                spEdit.putString("imagery", "custom")
                spEdit.putString("custom-imagery", imageryText)
                customImageryList.add(imageryText)
                spEdit.putStringSet("custom_imagery_list", customImageryList.toSet())
            } else {
                spEdit.putString("imagery", "Osm Carto")
            }
            spEdit.apply()
        }
        customImageryDialog.setNeutralButton(getString(R.string.cancel)) { _, _ -> }

        val vertialCustomImageryLinearLayout = LinearLayout(context)
        vertialCustomImageryLinearLayout.orientation = LinearLayout.VERTICAL

        val containerView = FrameLayout(requireContext())
        val textParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        textParams.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        textParams.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        textParams.topMargin = resources.getDimensionPixelSize(R.dimen.dialog_edit_text_margin)
        vertialCustomImageryLinearLayout.layoutParams = textParams

        imageryEditText.hint = getString(R.string.custom_slippy_url)

        vertialCustomImageryLinearLayout.addView(noCopyrightedMaterial)
        vertialCustomImageryLinearLayout.addView(imageryEditText)


        var i = 0
        for(customImagery in customImageryList) {
            val customImageryLinearLayout = LinearLayout(context)
            customImageryLinearLayout.orientation = LinearLayout.HORIZONTAL

            val button = Button(context)
            button.text = customImagery
            button.isSingleLine = true
            button.gravity = Gravity.CENTER_VERTICAL
            button.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 3f)


            customImageryLinearLayout.addView(button)

            val crossButton = ImageButton(context)
            crossButton.setImageResource(R.drawable.cross)
            crossButton.layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0.5f
            )
            crossButton.tag = i
            Log.i(TAG, "i: $i")
            Log.i(TAG, "crossButton.tag, ${crossButton.tag as Int}")
            button.setOnClickListener {
                imageryEditText.text.clear()
                imageryEditText.append(button.text)
            }

            crossButton.setOnClickListener {
                vertialCustomImageryLinearLayout.removeView(customImageryLinearLayout)
                customImageryList.removeAt(crossButton.tag as Int)
                spEdit.putStringSet("custom_imagery_list", customImageryList.toSet())
                spEdit.apply()
            }

            customImageryLinearLayout.addView(crossButton)

            vertialCustomImageryLinearLayout.addView(customImageryLinearLayout)
            i++
        }

        containerView.addView(vertialCustomImageryLinearLayout)

        customImageryDialog.setView(containerView)
        dialog = customImageryDialog.create()

        imageryEditText.requestFocus()


        sp = PreferenceManager.getDefaultSharedPreferences(context)

        val linearLayout =
            requireView().findViewById<LinearLayout>(R.id.choose_background_imagery_linear_layout)

        for (child in linearLayout.children) {
            try {
                val cardView = child as MaterialCardView
                for (child2 in cardView.children) {
                    val frameLayout = child2 as FrameLayout

                    frameLayout.setOnClickListener {
                        changeImagery(frameLayout)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, e.message.toString())
            }
        }

        val saveButton = requireView().findViewById<Button>(R.id.change_imagery_save_button)
        saveButton.setOnClickListener {
            onSavePressed()
        }

        imagery = sp.getString("imagery", "Osm Carto").toString()
        Log.i(TAG, imagery)
        when (imagery) {
            "Osm Carto" -> {
                val cardView =
                    requireView().findViewById<MaterialCardView>(R.id.osm_carto_card_view)
                cardView.strokeWidth = 5
                cardView.strokeColor = ContextCompat.getColor(requireContext(), R.color.border_blue)
            }
            "Mapbox Satellite" -> {
                val cardView =
                    requireView().findViewById<MaterialCardView>(R.id.mapbox_satellite_card_view)
                cardView.strokeWidth = 5
                cardView.strokeColor = ContextCompat.getColor(requireContext(), R.color.border_blue)
            }
            "Bing Satellite" -> {
                val cardView =
                    requireView().findViewById<MaterialCardView>(R.id.bing_satellite_card_view)
                cardView.strokeWidth = 5
                cardView.strokeColor = ContextCompat.getColor(requireContext(), R.color.border_blue)
            }
            "Esri Satellite" -> {
                val cardView =
                    requireView().findViewById<MaterialCardView>(R.id.esri_satellite_card_view)
                cardView.strokeWidth = 5
                cardView.strokeColor = ContextCompat.getColor(requireContext(), R.color.border_blue)
            }
            "Custom" -> {
                val cardView =
                    requireView().findViewById<MaterialCardView>(R.id.custom_imagery_card_view)
                cardView.strokeWidth = 5
                cardView.strokeColor = ContextCompat.getColor(requireContext(), R.color.border_blue)
            }
        }
    }

    private fun onSavePressed() {
        parentFragmentManager.popBackStack()
    }


    fun changeImagery(view: View) {

        val linearLayout =
            requireView().findViewById<LinearLayout>(R.id.choose_background_imagery_linear_layout)

        for (child in linearLayout.children) {
            try {
                val cardView = child as MaterialCardView
                cardView.strokeWidth = 0
            } catch (e: Exception) {
                Log.w(TAG, e.message.toString())
            }
        }
        try {
            val frameLayout = requireView().findViewById<FrameLayout>(view.id)
            val cardView: MaterialCardView = frameLayout.parent as MaterialCardView
            cardView.strokeColor = ContextCompat.getColor(requireContext(), R.color.border_blue)
            cardView.strokeWidth = 5
        } catch (e: Exception) {
            Log.w(TAG, "Error getting MaterialCardView: ${e.message.toString()}")
        }

        spEdit = sp.edit()
        when (view.id) {
            R.id.osm_carto_frame_view -> {
                if (imagery != "null") {
                    spEdit.putString("imagery", "Osm Carto")
                } else {
                    spEdit.putString("imagery", "Osm Carto")
                    Log.w(TAG, "imagery was null")
                }
                spEdit.apply()
            }
            R.id.mapbox_satellite_frame_layout -> {
                if (imagery != "null") {
                    spEdit.putString("imagery", "Mapbox Satellite")
                } else {
                    spEdit.putString("imagery", "Osm Carto")
                    Log.w(TAG, "imagery was null")
                }
                spEdit.apply()
            }
            R.id.bing_satellite_frame_layout -> {
                if (imagery != "null") {
                    spEdit.putString("imagery", "Bing Satellite")
                } else {
                    spEdit.putString("imagery", "Osm Carto")
                    Log.w(TAG, "imagery was null")
                }
                spEdit.apply()
            }
            R.id.esri_satellite_frame_layout -> {
                if (imagery != "null") {
                    spEdit.putString("imagery", "Esri Satellite")
                } else {
                    spEdit.putString("imagery", "Osm Carto")
                    Log.w(TAG, "imagery was null")
                }
                spEdit.apply()
            }
            R.id.custom_imagery_frame_layout -> {
                dialog.show()
                dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            }
        }
    }
}
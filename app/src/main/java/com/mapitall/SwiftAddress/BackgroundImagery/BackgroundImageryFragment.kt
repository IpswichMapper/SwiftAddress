package com.mapitall.SwiftAddress.BackgroundImagery

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.mapitall.SwiftAddress.R
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BackgroundImageryFragment : Fragment(R.layout.fragment_background_imagery) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val frameLayout = view.findViewById<FrameLayout>(R.id.change_background_imagery_frame_view)

        val currentImageryTextView = view.findViewById<TextView>(R.id.current_imagery_text_view)
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val imagery = sp.getString("imagery", "Osm Carto")!!
        currentImageryTextView.text = getString(R.string.current_imagery)

        frameLayout.setOnClickListener {
            val fragmentManager = parentFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val changeBackgroundImageryFragment = ChangeBackgroundImageryFragment()

            fragmentTransaction.addToBackStack("Background Imagery")
            fragmentTransaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out)
            fragmentTransaction.replace(R.id.fragmentContainer, changeBackgroundImageryFragment)
            fragmentTransaction.commit()
        }

        val chooseAreaButton: Button = view.findViewById(R.id.choose_area_button)
        chooseAreaButton.setOnClickListener {
            val fragmentManager = parentFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            val downloadTilesFragment = DownloadTilesFragment()

            fragmentTransaction.addToBackStack("Background Imagery")
            fragmentTransaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out)
            fragmentTransaction.replace(R.id.fragmentContainer, downloadTilesFragment)
            fragmentTransaction.commit()
        }

        val cantDownloadTilesTextView: TextView =
            view.findViewById(R.id.cant_download_tiles_text_view)
        if (imagery in arrayOf("Mapbox Satellite", "Esri Satellite")) {
            cantDownloadTilesTextView.visibility = View.GONE
            chooseAreaButton.isEnabled = true
            chooseAreaButton.alpha = 1f
        } else {
            cantDownloadTilesTextView.visibility = View.VISIBLE
            chooseAreaButton.isEnabled = false
            chooseAreaButton.alpha = 0.5f
        }

        val saveChanges: TextView = view.findViewById(R.id.save_background_imagery_changes)
        saveChanges.setOnClickListener {
            requireActivity().setResult(RESULT_OK)
            requireActivity().finish()
        }

        showCorrectImagery(imagery)
    }

    @SuppressLint("SetTextI18n")
    private fun showCorrectImagery(imagery: String) {

        val imageView: ImageView = requireView().findViewById(R.id.imagery_image)
        val textView: TextView = requireView().findViewById(R.id.current_imagery_text_view)
        when(imagery) {
            "Osm Carto" -> {
                imageView.setImageResource(R.drawable.osm_carto)
                textView.text = getString(R.string.current_imagery) + " " +
                        getString(R.string.osm_carto_imagery)
            }
            "Mapbox Satellite" -> {
                imageView.setImageResource(R.drawable.mapbox_satellite)
                textView.text = getString(R.string.current_imagery) + " " +
                        getString(R.string.mapbox_satellite_imagery)
            }
            "Esri Satellite" -> {
                imageView.setImageResource(R.drawable.mapbox_satellite)
                textView.text = getString(R.string.current_imagery) + " " +
                        getString(R.string.esri_satellite_imagery)
            }
            "Bing Satellite" -> {
                imageView.setImageResource(R.drawable.bing_satellite)
                textView.text = getString(R.string.current_imagery) + " " +
                        getString(R.string.bing_satellite_imagery)
            }
            "Custom" -> {
                imageView.setImageResource(R.drawable.mapbox_satellite)
                textView.text = getString(R.string.current_imagery) + " " +
                        getString(R.string.custom_imagery_text)
            }
        }
    }

}
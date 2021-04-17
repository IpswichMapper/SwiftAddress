package com.mapitall.SwiftAddress.BackgroundImagery

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mapitall.SwiftAddress.Map
import com.mapitall.SwiftAddress.R

class BackgroundImagery: AppCompatActivity() {

    private val args = Bundle()
    private val TAG = "BackgroundImageryActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_background_imagery)

        val backgroundImageryFragment = BackgroundImageryFragment()

        backgroundImageryFragment.arguments = args

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, backgroundImageryFragment)
            commit()
        }

    }
}
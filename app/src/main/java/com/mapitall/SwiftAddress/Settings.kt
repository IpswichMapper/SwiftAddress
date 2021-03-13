package com.mapitall.SwiftAddress

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import org.osmdroid.tileprovider.modules.SqlTileWriter

class Settings : AppCompatActivity(){

    private val TAG = "Settings"
    val contextLocal : Context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "onOptionsItemSelected")
        setResult(RESULT_OK, intent)
        finish()

        return true
    }

    override fun onBackPressed() {
        setResult(RESULT_OK, intent)
        finish()
    }


    class SettingsFragment : PreferenceFragmentCompat(),
            PreferenceManager.OnPreferenceTreeClickListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            Log.i("SettingsFragment", "preference.key : ${preference.key}")
            if (preference.key == "clear_cache") {
                val clearCacheDialog = AlertDialog.Builder(requireContext())
                clearCacheDialog.setTitle(getString(R.string.clear_cache))
                clearCacheDialog.setMessage(getString(R.string.clear_cache_question))

                clearCacheDialog.setPositiveButton(getString(R.string.clear_cache)) { _, _ ->
                    /*
                    val sp = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                    sp.putBoolean("clear_cache", true)
                    sp.apply()
                    */
                    Thread {
                        val tileWriter = SqlTileWriter()
                        tileWriter.purgeCache()
                    }
                }
                clearCacheDialog.setNeutralButton(getString(R.string.cancel)) { _, _ -> }

                clearCacheDialog.create().show()
            }
            return true
        }
    }
}
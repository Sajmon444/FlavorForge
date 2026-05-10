package pl.edu.anstar.flavorforge

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var languageSpinner: Spinner
    private lateinit var unitSpinner: Spinner
    private var isInitialSelection = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        languageSpinner = findViewById(R.id.languageSpinner)
        unitSpinner = findViewById(R.id.unitSpinner)
        
        setupLanguageSpinner()
        setupUnitSpinner()

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnApplySettings)?.setOnClickListener {
            Toast.makeText(this, getString(R.string.apply_settings), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf(getString(R.string.lang_en), getString(R.string.lang_pl))
        
        val adapter = ArrayAdapter(this, R.layout.spinner_item, languages)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        languageSpinner.adapter = adapter

        val appLocales = AppCompatDelegate.getApplicationLocales()
        val currentLocale = if (appLocales.isEmpty) "pl" else appLocales[0]?.language ?: "pl"

        if (currentLocale == "pl") {
            languageSpinner.setSelection(1)
        } else {
            languageSpinner.setSelection(0)
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInitialSelection) {
                    isInitialSelection = false
                    return
                }

                val selectedLanguageCode = if (position == 1) "pl" else "en"
                val currentLanguageCode = if (AppCompatDelegate.getApplicationLocales().isEmpty) "" else AppCompatDelegate.getApplicationLocales()[0]?.language

                if (selectedLanguageCode != currentLanguageCode) {
                    setAppLocale(selectedLanguageCode)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupUnitSpinner() {
        val units = arrayOf(getString(R.string.unit_metric), getString(R.string.unit_imperial))
        
        val adapter = ArrayAdapter(this, R.layout.spinner_item, units)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        unitSpinner.adapter = adapter

        // Domyślnie Metric (pierwsza pozycja)
        unitSpinner.setSelection(0)

        unitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setAppLocale(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}

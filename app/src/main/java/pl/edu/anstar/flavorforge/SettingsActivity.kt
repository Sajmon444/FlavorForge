package pl.edu.anstar.flavorforge

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.AndroidEntryPoint
import pl.edu.anstar.flavorforge.data.local.SessionManager
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var languageSpinner: Spinner
    private lateinit var unitSpinner: Spinner
    private lateinit var themeSpinner: Spinner

    private lateinit var timeSeekBar: SeekBar
    private lateinit var tvTimeLabel: TextView
    private lateinit var caloriesSeekBar: SeekBar
    private lateinit var tvCaloriesLabel: TextView

    private var isInitialSelection = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        languageSpinner = findViewById(R.id.languageSpinner)
        unitSpinner = findViewById(R.id.unitSpinner)
        themeSpinner = findViewById(R.id.themeSpinner)

        timeSeekBar = findViewById(R.id.timeSeekBar)
        tvTimeLabel = findViewById(R.id.tvTimeLabel)
        caloriesSeekBar = findViewById(R.id.caloriesSeekBar)
        tvCaloriesLabel = findViewById(R.id.tvCaloriesLabel)

        setupLanguageSpinner()
        setupUnitSpinner()
        setupThemeSpinner()
        setupTimeSeekBar()
        setupCaloriesSeekBar()

        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnApplySettings)?.setOnClickListener {
            Toast.makeText(this, getString(R.string.apply_settings), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // -------------------------------
    // LANGUAGE
    // -------------------------------
    private fun setupLanguageSpinner() {
        val languages = arrayOf(getString(R.string.lang_en), getString(R.string.lang_pl))

        val adapter = ArrayAdapter(this, R.layout.spinner_item, languages)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        languageSpinner.adapter = adapter

        val prefs = sessionManager.getSettingsPrefs(this)
        val savedLang = prefs.getString("app_lang", null)

        if (savedLang == null) {
            val appLocales = AppCompatDelegate.getApplicationLocales()
            val currentLocale = if (appLocales.isEmpty) "pl" else appLocales[0]?.language ?: "pl"
            languageSpinner.setSelection(if (currentLocale == "pl") 1 else 0)
        } else {
            languageSpinner.setSelection(if (savedLang == "pl") 1 else 0)
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isInitialSelection) {
                    isInitialSelection = false
                    return
                }

                val selectedLanguageCode = if (position == 1) "pl" else "en"
                val savedLangCode = prefs.getString("app_lang", "")

                if (selectedLanguageCode != savedLangCode) {
                    prefs.edit().putString("app_lang", selectedLanguageCode).apply()
                    sessionManager.applySettings(this@SettingsActivity)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // -------------------------------
    // UNITS
    // -------------------------------
    private fun setupUnitSpinner() {
        val units = arrayOf(getString(R.string.unit_metric), getString(R.string.unit_imperial))

        val adapter = ArrayAdapter(this, R.layout.spinner_item, units)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        unitSpinner.adapter = adapter

        unitSpinner.setSelection(0)

        unitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // tu możesz dodać zapis do prefs jeśli chcesz
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // -------------------------------
    // THEME (LIGHT / DARK / SYSTEM)
    // -------------------------------
    private var isThemeInitialSelection = true

    private fun setupThemeSpinner() {
        val themes = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )

        val adapter = ArrayAdapter(this, R.layout.spinner_item, themes)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        themeSpinner.adapter = adapter

        val prefs = sessionManager.getSettingsPrefs(this)
        val savedTheme = prefs.getString("app_theme", "system")

        // 🔥 Ustawiamy spinner zgodnie z zapisanym motywem
        when (savedTheme) {
            "light" -> themeSpinner.setSelection(0)
            "dark" -> themeSpinner.setSelection(1)
            else -> themeSpinner.setSelection(2) // SYSTEM
        }

        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                if (isThemeInitialSelection) {
                    isThemeInitialSelection = false
                    return
                }

                val selectedTheme = when (position) {
                    0 -> "light"
                    1 -> "dark"
                    else -> "system"
                }

                prefs.edit().putString("app_theme", selectedTheme).apply()
                sessionManager.applySettings(this@SettingsActivity)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // -------------------------------
    // PREPARATION TIME SEEKBAR
    // -------------------------------
    private fun setupTimeSeekBar() {
        val prefs = sessionManager.getSettingsPrefs(this)
        // Default max preparation time is 180 min -> progress = 12
        val savedProgress = prefs.getInt("settings_prep_time", 12)
        timeSeekBar.progress = savedProgress

        // Display current progress value
        val minutes = savedProgress * 15
        tvTimeLabel.text = getString(R.string.settings_time_label, minutes)

        timeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val currentMinutes = progress * 15
                tvTimeLabel.text = getString(R.string.settings_time_label, currentMinutes)
                prefs.edit().putInt("settings_prep_time", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    // -------------------------------
    // CALORIES SEEKBAR
    // -------------------------------
    private fun setupCaloriesSeekBar() {
        val prefs = sessionManager.getSettingsPrefs(this)
        // Default max calories is 3000 kcal -> progress = 54
        val savedProgress = prefs.getInt("settings_calories", 54)
        caloriesSeekBar.progress = savedProgress

        // Display current progress value
        val kcal = 300 + savedProgress * 50
        tvCaloriesLabel.text = getString(R.string.settings_calories, kcal)

        caloriesSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val currentKcal = 300 + progress * 50
                tvCaloriesLabel.text = getString(R.string.settings_calories, currentKcal)
                prefs.edit().putInt("settings_calories", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}

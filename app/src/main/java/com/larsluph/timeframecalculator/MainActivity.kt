package com.larsluph.timeframecalculator

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    /**
     * is timestamp displayed or datetime
     */
    private var isTimestamp: Boolean = false
        set(value) {
            field = value
            updateDisplay() // update layout when display type changes
        }

    /**
     * is save needed when app loses focus
     */
    private var saveNeeded: Boolean = false

    /**
     * currently selected timestamp
     */
    private var loadedTimeFrame: Long = 0
        /**
         * update layout when selection changes
         */
        set(value) {
            field = value
            updateDisplay()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // setup click listeners
        findViewById<TextView>(R.id.textview_data).setOnLongClickListener { loadCustomTimeframe() }

        findViewById<TextView>(R.id.button_save).setOnClickListener { loadCurrentTime() }
        findViewById<TextView>(R.id.button_load).setOnClickListener { loadCustomTimeframe() }
        findViewById<TextView>(R.id.button_calc).setOnClickListener { computeTimeframes() }

        findViewById<Button>(R.id.button_toggle_main).setOnClickListener { toggleTimeFormat() }
    }

    override fun onResume() {
        super.onResume()

        // load timestamp if one is saved else select present time
        if (isTimestampSaved()) {
            loadSavedTimeframe()
        } else {
            loadCurrentTime()
        }
        saveNeeded = false
    }

    override fun onPause() {
        super.onPause()

        // save selected timestamp if needed
        if (saveNeeded) {
            saveTimeframe()
        }
    }

    /**
     * creates actionbar menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar, menu)
        return true
    }

    /**
     * handles actionbar menu clicks
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_reset -> resetTimeFrame()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateTextView(data: CharSequence) {
        findViewById<TextView>(R.id.textview_data).text = data
    }

    /**
     * format Calendar object before updating TextView
     */
    private fun updateTextView(data: Calendar) {
        // fetch data from Calendar instance
        val day = data.get(Calendar.DAY_OF_MONTH)
        val month = data.get(Calendar.MONTH) + 1
        val year = data.get(Calendar.YEAR)

        val hour = data.get(Calendar.HOUR_OF_DAY)
        val minute = data.get(Calendar.MINUTE)
        val second = data.get(Calendar.SECOND)
        val ms = data.get(Calendar.MILLISECOND)

        // format string to match localization clock format
        updateTextView(Html.fromHtml(getString(R.string.datetime_format, day, month, year, hour, minute, second, ms), Html.FROM_HTML_MODE_LEGACY))
    }

    /**
     * selects update method based on user choice
     */
    private fun updateDisplay() {
        if (isTimestamp) {
            updateTextView(loadedTimeFrame.toString())
        } else {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = loadedTimeFrame
            updateTextView(calendar)
        }
    }

    /**
     * called with toggle button listener
     */
    private fun toggleTimeFormat() {
        isTimestamp = !isTimestamp
    }

    /**
     * update selected timestamp to present time
      */
    private fun loadCurrentTime() {
        saveNeeded = true
        loadedTimeFrame = Calendar.getInstance().timeInMillis
    }

    /**
     * opens a popup to select date and time or timestamp to select
     */
    private fun loadCustomTimeframe(): Boolean {
        saveNeeded = true
        if (isTimestamp) {
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER

            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.ask_timestamp_dialog))
                    .setView(input)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        loadedTimeFrame = input.text.toString().toLong()
                    }
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
        } else {
            // get current time
            val currentDateTime = Calendar.getInstance()
            val startYear = currentDateTime.get(Calendar.YEAR)
            val startMonth = currentDateTime.get(Calendar.MONTH)
            val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
            val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
            val startMinute = currentDateTime.get(Calendar.MINUTE)

            // open datepicker dialog
            DatePickerDialog(this, { _, year, month, day ->
                // then open timepicker dialog
                TimePickerDialog(this, { _, hour, minute ->
                    // and save the selected timeframe
                    loadedTimeFrame = Calendar.Builder()
                        .setDate(year, month, day)
                        .setTimeOfDay(hour, minute, 0)
                        .build()
                        .timeInMillis
                }, startHour, startMinute, false).show()
            }, startYear, startMonth, startDay).show()
        }
        return true
    }

    /**
     * launch ComputeActivity to display the calculation between the present time and the selected datetime
     */
    private fun computeTimeframes() {
        intent = Intent(this, ComputeActivity::class.java)
        intent.putExtra(getString(R.string.stampExtraKey), loadedTimeFrame)
        startActivity(intent)
    }

    /**
     * checks whether timeframe is saved in SharedPreferences
     */
    private fun isTimestampSaved(): Boolean {
        val sp = getSharedPreferences(getString(R.string.packageName), MODE_PRIVATE)
        val exists = sp.all.containsKey(getString(R.string.timestampPreferenceKey))
        Log.d("isExists", exists.toString())
        return exists
    }

    /**
     * loads timeframe saved in SharedPreferences
     */
    private fun loadSavedTimeframe() {
        val sp = getSharedPreferences(getString(R.string.packageName), MODE_PRIVATE)
        if (isTimestampSaved()) {
            loadedTimeFrame = sp.getLong(getString(R.string.timestampPreferenceKey), 0)
        } else {
            loadCurrentTime()
        }
        saveNeeded = false
    }

    /**
     * saves timeframe in SharedPreferences
     */
    private fun saveTimeframe() {
        val sp = getSharedPreferences(getString(R.string.packageName), MODE_PRIVATE).edit()
        sp.putLong(getString(R.string.timestampPreferenceKey), loadedTimeFrame)
        sp.apply()
    }

    /**
     * clears saved timeframe in SharedPreferences
     */
    private fun resetTimeFrame(): Boolean {
        getSharedPreferences(getString(R.string.packageName), MODE_PRIVATE).edit().remove(getString(R.string.timestampPreferenceKey)).apply()
        loadCurrentTime()
        saveNeeded = false
        return true
    }
}

package com.larsluph.timeframecalculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ComputeActivity : AppCompatActivity() {

    enum class FieldIDs {
        YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLIS
    }

    private val millisToSeconds = 1000L
    private val millisToMinutes = millisToSeconds*60
    private val millisToHours = millisToMinutes*60
    private val millisToDays = millisToHours*24

    var isAlreadyLooping = false

    private var isTotal = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compute)

        findViewById<Button>(R.id.button_toggle_compute).setOnClickListener { toggleTotal() }

        Thread(ComputeLooper(this)).start()
    }

    private fun toggleTotal() {
        isTotal = !isTotal
    }

    /*
    fun updateLoop() {
        isAlreadyLooping = true
        val calendar = Calendar.getInstance()
        timestamp1 = intent.extras!!.getLong(getString(R.string.stampExtraKey))
        timestamp2 = calendar.timeInMillis

        val timestampDelta = timestamp2 - timestamp1

        if (isTotal) {
            val years = getYears(timestampDelta)[0]
            val months = getMonths(timestampDelta)[0]
            val days = getDays(timestampDelta)[0]
            val hours = getHours(timestampDelta)[0]
            val minutes = getMinutes(timestampDelta)[0]
            val seconds = getSeconds(timestampDelta)[0]

            updateField(FieldIDs.YEAR, years)
            updateField(FieldIDs.MONTH, months)
            updateField(FieldIDs.DAY, days)
            updateField(FieldIDs.HOUR, hours)
            updateField(FieldIDs.MINUTE, minutes)
            updateField(FieldIDs.SECOND, seconds)
            updateField(FieldIDs.MILLIS, timestampDelta)
        } else {
            val yearsArray = getYears(timestampDelta)
            val monthsArray = getMonths(yearsArray[1])
            val daysArray = getDays(monthsArray[1])
            val hoursArray = getHours(daysArray[1])
            val minutesArray = getMinutes(hoursArray[1])
            val secondsArray = getSeconds(minutesArray[1])

            updateField(FieldIDs.YEAR, yearsArray[0])
            updateField(FieldIDs.MONTH, monthsArray[0])
            updateField(FieldIDs.DAY, daysArray[0])
            updateField(FieldIDs.HOUR, hoursArray[0])
            updateField(FieldIDs.MINUTE, minutesArray[0])
            updateField(FieldIDs.SECOND, secondsArray[0])
            updateField(FieldIDs.MILLIS, secondsArray[1])
        }
        isAlreadyLooping = false
    }*/

    fun updateLoop() {
        isAlreadyLooping = true
        val calendar1 = Calendar.getInstance()
        calendar1.timeInMillis = intent.extras!!.getLong(getString(R.string.stampExtraKey))

        val calendar2 = Calendar.getInstance()

        val (years, months, days, hours, minutes, seconds, millis) = computeDateTime(calendar1, calendar2)

        updateField(FieldIDs.YEAR, years)
        updateField(FieldIDs.MONTH, months)
        updateField(FieldIDs.DAY, days)
        updateField(FieldIDs.HOUR, hours)
        updateField(FieldIDs.MINUTE, minutes)
        updateField(FieldIDs.SECOND, seconds)
        updateField(FieldIDs.MILLIS, millis)

        isAlreadyLooping = false
    }

    private fun updateField(id: FieldIDs, data: Long) {
        when (id) {
            FieldIDs.YEAR -> findViewById<TextView>(R.id.years).text = getString(R.string.compute_year, data)
            FieldIDs.MONTH -> findViewById<TextView>(R.id.months).text = getString(R.string.compute_month, data)
            FieldIDs.DAY -> findViewById<TextView>(R.id.days).text = getString(R.string.compute_day, data)
            FieldIDs.HOUR -> findViewById<TextView>(R.id.hours).text = getString(R.string.compute_hour, data)
            FieldIDs.MINUTE -> findViewById<TextView>(R.id.minutes).text = getString(R.string.compute_minute, data)
            FieldIDs.SECOND -> findViewById<TextView>(R.id.seconds).text = getString(R.string.compute_second, data)
            FieldIDs.MILLIS -> findViewById<TextView>(R.id.millis).text = getString(R.string.compute_millis, data)
        }
    }

    private data class DateTime(val years: Long, val months: Long, val days: Long, val hours: Long, val minutes: Long, val seconds: Long, val millis: Long)
    private data class DivMod(val div: Long, val mod: Long)
    private fun divmod(val1: Long, val2: Long): DivMod {
        return DivMod(val1 / val2, val1 % val2)
    }

    private fun computeDateTime(data1: Calendar, data2: Calendar): DateTime {
        val timestampDelta = data2.timeInMillis - data1.timeInMillis

        if (isTotal) {
            var years = data2.get(Calendar.YEAR) - data1.get(Calendar.YEAR) // compute year difference

            val temp = data2.clone() as Calendar // clone data2
            temp.set(Calendar.YEAR, data1.get(Calendar.YEAR)) // and nullify year to compare it

            if (temp.before(data1)) years-- // if date/month of data2 is before data1, year cycle not completed => years--

            // same for month
            var months = 12*years + (data2.get(Calendar.MONTH) - data1.get(Calendar.MONTH))

            if (data1.get(Calendar.DAY_OF_MONTH) > data2.get(Calendar.DAY_OF_MONTH)) months-- // if day of data2 is before data1, month cycle not completed => months--

            val days = timestampDelta / millisToDays
            val hours = timestampDelta / millisToHours
            val minutes = timestampDelta / millisToMinutes
            val seconds = timestampDelta / millisToSeconds

            return DateTime(years.toLong(), months.toLong(), days, hours, minutes, seconds, timestampDelta)

        } else {
            val (_seconds, millis) = divmod(timestampDelta, 60)
            val (_minutes, seconds) = divmod(_seconds, 60)
            val (_hours, minutes) = divmod(_minutes, 60)
            val (days, hours) = divmod(_hours, 24)

            // TODO("Fix months / years computation")
            // val yearsArray = getYears(timestampDelta)
            // val monthsArray = getMonths(yearsArray[1])
            val months = data2.get(Calendar.MONTH) - data1.get(Calendar.MONTH)
            val years = data2.get(Calendar.YEAR) - data1.get(Calendar.YEAR)

            return DateTime(years.toLong(), months.toLong(), days, hours, minutes, seconds, millis)
        }

    }
}

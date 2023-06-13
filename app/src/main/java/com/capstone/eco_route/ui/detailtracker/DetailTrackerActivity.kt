package com.capstone.eco_route.ui.detailtracker

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.capstone.eco_route.databinding.ActivityDetailTrackerBinding
import com.capstone.eco_route.ui.homepage.HomepageActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION", "UNUSED_EXPRESSION")
class DetailTrackerActivity : AppCompatActivity() {

    private val binding: ActivityDetailTrackerBinding by lazy {
        ActivityDetailTrackerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val dateFormat = "dd MMMM yyyy"
            val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.US)
            binding.tripDate.text = simpleDateFormat.format(calendar.time)
        }

        binding.groupDate.setOnClickListener {
            DatePickerDialog(this@DetailTrackerActivity, dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.groupYear.setOnClickListener {
            showDialog(DATE_DIALOG_ID)
        }

        binding.buttonSave.setOnClickListener {
            showSaveCalculateDialog()
        }

        binding.buttonCancel.setOnClickListener {
            showCancelCalculateDialog()
        }

    }

    private fun showSaveCalculateDialog() {
        AlertDialog.Builder(
            this@DetailTrackerActivity
        ).apply {
            setTitle("Save")
            setMessage("Are you sure want to save the calculation data?")
            setPositiveButton("Yes") { _, _ ->
                val intent = Intent(
                    this@DetailTrackerActivity,
                    HomepageActivity::class.java
                )
                startActivity(intent)
                Toast.makeText(
                    this@DetailTrackerActivity,
                    "Successfully save the calculation",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            setNegativeButton("No") {_, _ ->
                // Do Nothing
            }
            create()
            show()
        }
    }

    private fun showCancelCalculateDialog() {
        AlertDialog.Builder(
            this@DetailTrackerActivity
        ).apply {
            setTitle("Cancel")
            setMessage("Are you sure want to cancel the calculation?")
            setPositiveButton("Yes") { _, _ ->
                val intent = Intent(
                    this@DetailTrackerActivity,
                    HomepageActivity::class.java
                )
                startActivity(intent)
                finish()
            }
            setNegativeButton("No") {_, _ ->
                // Do Nothing
            }
            create()
            show()
        }
    }


    private var yearDate: Int = 0
    private var monthDate: Int = 0
    private var dayDate: Int = 0

    private val listener = DatePickerDialog.OnDateSetListener {_, year, month, day ->
        yearDate = year
        monthDate = month
        dayDate = day
        updateDate()

    }
    @Deprecated("Deprecated in Java")
    override fun onCreateDialog(id: Int): Dialog {
        return when (id) {
            DATE_DIALOG_ID -> {
                yearDialogBuilder()
            }
            else -> super.onCreateDialog(id)
        }
    }

    private fun updateDate() {
//        val localMonth = monthDate + 1
//        if (localMonth < 10) "0$localMonth" else localMonth.toString()
        val localYear = yearDate.toString().substring(2)
        binding.tripYearValue.text = StringBuilder().append(localYear).append("")
        showDialog(DATE_DIALOG_ID)
    }

    private fun yearDialogBuilder(): DatePickerDialog {
        val datePickerDialog = DatePickerDialog(
            this,
            listener,
            yearDate,
            monthDate,
            dayDate
        )
        try {
            val datePickerDialogFields = datePickerDialog.javaClass.declaredFields
            for (datePickerDialogField in datePickerDialogFields) {
                if (datePickerDialogField.name == "mDatePicker") {
                    datePickerDialogField.isAccessible = true
                    val datePicker = datePickerDialogField.get(datePickerDialog) as DatePicker
                    val datePickerFields = datePickerDialogField.type.declaredFields
                    for (datePickerField in  datePickerFields) {
                        Log.i("dateTest", datePickerField.name)
                        if ("mDayPicker" == datePickerField.name || "mDaySpinner" == datePickerField.name) {
                            datePickerField.isAccessible = true
                            val dayPicker = datePickerField.get(datePicker) as View
                            dayPicker.visibility = View.GONE
                        }
                    }
                }
            }
        } catch (_: Exception) {

        }

        return datePickerDialog
    }

    companion object {
        private const val DATE_DIALOG_ID = 1
    }

}
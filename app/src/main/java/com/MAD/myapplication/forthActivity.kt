package com.MAD.myapplication

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.MAD.myapplication.R
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class forthActivity : AppCompatActivity() {

    private val apiKey = "dc6d0c4421d5cbac3e13f7785dbbef38"

    private lateinit var locationAutoCompleteTextView: AutoCompleteTextView
    private lateinit var searchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forth)

        val backButton = findViewById<Button>(R.id.backbtn)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        locationAutoCompleteTextView = findViewById(R.id.locationAutoCompleteTextView)
        searchButton = findViewById(R.id.searchButton)

        // Set up adapter for AutoCompleteTextView
        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            arrayOf("City1", "City2", "City3") // Add your own list of cities
        )
        locationAutoCompleteTextView.setAdapter(adapter)

        searchButton.setOnClickListener {
            val selectedLocation = locationAutoCompleteTextView.text.toString()
            if (selectedLocation.isNotEmpty()) {
                WeatherTask().execute(selectedLocation)
            }
        }
    }

    inner class WeatherTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String? {
            val response: String?
            try {
                val city = params[0] ?: return null
                // API URL for 5-day weather forecast
                val apiUrl = "https://api.openweathermap.org/data/2.5/forecast?q=$city&appid=$apiKey"

                // Make the API call and get the response
                response = URL(apiUrl).readText(Charsets.UTF_8)
            } catch (e: Exception) {
                return null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                // Parse the JSON response
                val jsonObj = JSONObject(result)
                val list = jsonObj.getJSONArray("list")

                // Remove previous forecast TextViews
                val forecastContainer = findViewById<LinearLayout>(R.id.forecastContainer)
                forecastContainer.removeAllViews()

                // Display the forecast for the next 4 days
                for (i in 0 until 4) {
                    if (i * 8 < list.length()) {
                        val day = list.getJSONObject(i * 8) // Data for every 3 hours, so skip to the next day
                        val main = day.getJSONObject("main")
                        val temp = main.getString("temp")

                        val dateText = getDateText(day.getString("dt"))
                        val forecast = "$dateText: $temp °C"

                        // Create a new TextView for each forecast
                        val forecastTextView = TextView(this@forthActivity)
                        forecastTextView.text = forecast

                        // Add the TextView to the forecastContainer
                        forecastContainer.addView(forecastTextView)
                    }
                }

                // Display the selected location
                val locationTextView = TextView(this@forthActivity)
                locationTextView.text = "Location: ${locationAutoCompleteTextView.text}"
                forecastContainer.addView(locationTextView)

            } catch (e: Exception) {
                // Handle exception (e.g., JSON parsing error)
            }
        }

        private fun getDateText(timestamp: String): String {
            // Convert timestamp to date format (you can customize the format)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = Date(timestamp.toLong() * 1000)
            return sdf.format(date)
        }
    }
}
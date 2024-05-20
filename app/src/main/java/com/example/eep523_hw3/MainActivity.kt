package com.example.eep523_hw3

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    // Initialize UI components and set the default city
    private lateinit var cityInput: EditText
    private lateinit var fetchButton: Button
    private lateinit var errorMessage: TextView
    private val defaultCity = "Seattle,US"

    // Variables for city name and API key
    private var city: String = defaultCity
    private val apiKey: String = "1813c046a30fb98fccca28c4d3bc94af"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind UI elements
        cityInput = findViewById(R.id.inputCity)
        fetchButton = findViewById(R.id.submitButton)
        errorMessage = findViewById(R.id.errorText)

        // Set default text in the input field
        cityInput.setText(defaultCity)

        // Set up a click listener for the fetch button
        fetchButton.setOnClickListener {
            // Retrieve text from the input field
            val inputCity = cityInput.text.toString()

            // Determine the city to use based on the input
            city = if (inputCity.isBlank()) defaultCity else inputCity

            // Execute the task to fetch weather data
            WeatherTask().execute()
        }

        // Execute the weather data fetching task on initial load
        WeatherTask().execute()
    }

    // Inner class to handle the asynchronous task of fetching weather data
    inner class WeatherTask : AsyncTask<String, Void, String>() {
        // Prepare for the task before execution
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            errorMessage.visibility = View.GONE
        }

        // Perform the background operation to fetch data
        override fun doInBackground(vararg params: String?): String? {
            var responseString: String?
            try {
                responseString = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$apiKey").readText(
                    Charsets.UTF_8
                )
            } catch (e: Exception) {
                responseString = null
            }
            return responseString
        }

        // Process the data after fetching it
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                // Verify the result is not null or empty
                if (result.isNullOrEmpty()) {
                    throw Exception("No response from server")
                }

                // Parse the JSON object
                val jsonResponse = JSONObject(result)
                val main = jsonResponse.getJSONObject("main")
                val system = jsonResponse.getJSONObject("sys")
                val wind = jsonResponse.getJSONObject("wind")
                val weather = jsonResponse.getJSONArray("weather").getJSONObject(0)

                // Extract and display the weather details
                val updateTime: Long = jsonResponse.getLong("dt")
                val updateText = "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updateTime * 1000))
                val temperature = main.getString("temp") + "°C"
                val minTemp = "Min Temp: " + main.getString("temp_min") + "°C"
                val maxTemp = "Max Temp: " + main.getString("temp_max") + "°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunriseTime: Long = system.getLong("sunrise")
                val sunsetTime: Long = system.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val location = jsonResponse.getString("name") + ", " + system.getString("country")

                // Populate the UI with the extracted data
                findViewById<TextView>(R.id.address).text = location
                findViewById<TextView>(R.id.updated_at).text = updateText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temperature
                findViewById<TextView>(R.id.temp_min).text = minTemp
                findViewById<TextView>(R.id.temp_max).text = maxTemp
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunriseTime * 1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunsetTime * 1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity

                // Reveal the main content and hide the loader
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            } catch (e: Exception) {
                // Display error message in case of failure
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                errorMessage.text = "Error: ${e.message}"
                errorMessage.visibility = View.VISIBLE
            }
        }
    }
}

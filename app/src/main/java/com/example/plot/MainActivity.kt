package com.example.plot

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private lateinit var accelerometerChartX: LineChart
    private lateinit var accelerometerChartY: LineChart
    private lateinit var accelerometerChartZ: LineChart
    private lateinit var filteredAccelerometerChartX: LineChart
    private lateinit var filteredAccelerometerChartY: LineChart
    private lateinit var filteredAccelerometerChartZ: LineChart
    private lateinit var gyroscopeChartX: LineChart
    private lateinit var gyroscopeChartY: LineChart
    private lateinit var gyroscopeChartZ: LineChart

    private var entryCount = 0
    private val alpha = 0.9f // Low pass filter alpha
    private var filteredAccelValues = FloatArray(3) { 0f } // Initialize filtered values to 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Get accelerometer and gyroscope sensors
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Initialize charts
        accelerometerChartX = findViewById(R.id.accelerometerChartX)
        accelerometerChartY = findViewById(R.id.accelerometerChartY)
        accelerometerChartZ = findViewById(R.id.accelerometerChartZ)
        filteredAccelerometerChartX = findViewById(R.id.filteredAccelerometerChartX)
        filteredAccelerometerChartY = findViewById(R.id.filteredAccelerometerChartY)
        filteredAccelerometerChartZ = findViewById(R.id.filteredAccelerometerChartZ)
        gyroscopeChartX = findViewById(R.id.gyroscopeChartX)
        gyroscopeChartY = findViewById(R.id.gyroscopeChartY)
        gyroscopeChartZ = findViewById(R.id.gyroscopeChartZ)

        // Customize charts
        setupChart(accelerometerChartX, "Raw Accelerometer X Axis")
        setupChart(accelerometerChartY, "Raw Accelerometer Y Axis")
        setupChart(accelerometerChartZ, "Raw Accelerometer Z Axis")
        setupChart(filteredAccelerometerChartX, "Filtered Accelerometer X Axis")
        setupChart(filteredAccelerometerChartY, "Filtered Accelerometer Y Axis")
        setupChart(filteredAccelerometerChartZ, "Filtered Accelerometer Z Axis")
        setupChart(gyroscopeChartX, "Gyroscope X Axis")
        setupChart(gyroscopeChartY, "Gyroscope Y Axis")
        setupChart(gyroscopeChartZ, "Gyroscope Z Axis")

        // Register sensor listeners
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this example
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    updateAccelerometerCharts(it.values)
                }
                Sensor.TYPE_GYROSCOPE -> {
                    updateGyroscopeCharts(it.values)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister sensor listeners
        sensorManager.unregisterListener(this)
    }

    private fun setupChart(chart: LineChart, title: String) {
        chart.apply {
            description = Description().apply { text = title }
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setDrawGridBackground(false)
            axisLeft.setDrawGridLines(false)
            axisRight.setDrawGridLines(false)
            xAxis.setDrawGridLines(false)
            legend.isEnabled = false
        }
    }

    private fun updateAccelerometerCharts(values: FloatArray) {
        // Update raw data charts
        entryCount++
        if (entryCount > 300) {
            entryCount = 0
            resetChart(accelerometerChartX)
            resetChart(accelerometerChartY)
            resetChart(accelerometerChartZ)
            resetChart(filteredAccelerometerChartX)
            resetChart(filteredAccelerometerChartY)
            resetChart(filteredAccelerometerChartZ)
        }
        updateChart(accelerometerChartX, values[0])
        updateChart(accelerometerChartY, values[1])
        updateChart(accelerometerChartZ, values[2])

        // Apply low pass filter
        filteredAccelValues[0] = lowPassFilter(values[0], filteredAccelValues[0])
        filteredAccelValues[1] = lowPassFilter(values[1], filteredAccelValues[1])
        filteredAccelValues[2] = lowPassFilter(values[2], filteredAccelValues[2])

        // Update filtered data charts
        updateChart(filteredAccelerometerChartX, filteredAccelValues[0])
        updateChart(filteredAccelerometerChartY, filteredAccelValues[1])
        updateChart(filteredAccelerometerChartZ, filteredAccelValues[2])
    }

    private fun updateGyroscopeCharts(values: FloatArray) {
        entryCount++
        if (entryCount > 300){
            entryCount = 0
            resetChart(gyroscopeChartX)
            resetChart(gyroscopeChartY)
            resetChart(gyroscopeChartZ)
        }
        updateChart(gyroscopeChartX, values[0])
        updateChart(gyroscopeChartY, values[1])
        updateChart(gyroscopeChartZ, values[2])
    }

    private fun lowPassFilter(value: Float, prevValue: Float): Float {
        return prevValue * alpha + value *(1-alpha)
    }

    private fun updateChart(chart: LineChart, value: Float) {
        val data = chart.data
        if (data == null) {
            val set = LineDataSet(mutableListOf(Entry(entryCount.toFloat(), value)), "Data")
            set.setDrawCircles(false)
            set.setDrawValues(false)
            val lineData = LineData(set)
            chart.data = lineData
        } else {
            var set = data.getDataSetByIndex(0) as? LineDataSet
            if (set == null) {
                set = LineDataSet(mutableListOf(Entry(entryCount.toFloat(), value)), "Data")
                set.setDrawCircles(false)
                set.setDrawValues(false)
                data.addDataSet(set)
            } else {
                set.addEntry(Entry(entryCount.toFloat(), value))
                data.notifyDataChanged()
                chart.notifyDataSetChanged()
            }
        }
        chart.invalidate()
    }

    private fun resetChart(chart: LineChart) {
        chart.data = null
        chart.invalidate()
    }
}

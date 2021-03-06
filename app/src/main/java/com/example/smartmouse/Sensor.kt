package com.example.smartmouse

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.util.Log

class Sensor(context: Context) : SensorEventListener {
    private var handler: Handler = Handler()
    private var sensorManager: SensorManager =
        context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometerReading = FloatArray(3)
    private val linearReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var accelerations = Matrix(3, 1)
    private var displacements = Matrix(3, 1)

    private var lowpass = Matrix(3, 1)
    private var highpass = Matrix(3, 1)
    private var preVelocity = Matrix(3, 1)
    private var velocity = Matrix(3, 1)
    private var timeStamp: Pair<Double, Double> = Pair(0.0, 0.0)

    private val filterCoefficient: Float = 0.9F

    private var counter: Int = 0


    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && counter == 0) {
            when {
                (event.sensor.type == Sensor.TYPE_ACCELEROMETER) -> System.arraycopy(
                    event.values,
                    0,
                    accelerometerReading,
                    0,
                    accelerometerReading.size
                )
                (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) -> {
                    System.arraycopy(event.values, 0, linearReading, 0, linearReading.size)
                    timeStamp = Pair(timeStamp.second, System.currentTimeMillis().toDouble())
                }
                //(event.sensor.type == Sensor.TYPE_ORIENTATION) -> orientations?.add(data)
                (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) -> System.arraycopy(
                    event.values,
                    0,
                    magnetometerReading,
                    0,
                    magnetometerReading.size
                )
            }
            if (!accelerometerReading.contentEquals(FloatArray(3)) && !magnetometerReading.contentEquals(FloatArray(3)) && !linearReading.contentEquals(FloatArray(3)))
            {
                calculate()
            }
        }
        if (counter != 0){
            counter -= 1
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


    fun enableSensor() {
        counter = 100
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    fun disableSensor() {
        sensorManager.unregisterListener(this)
    }

    private fun calculate() {
        handler.post {
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                accelerometerReading,
                magnetometerReading
            )
            SensorManager.getOrientation(rotationMatrix, orientation)
            accelerations.copy(
                Matrix.multipled(
                    Matrix(3, 3, rotationMatrix),
                    Matrix(3, 1, linearReading)
                )
            )
            lowpass.copy(
                Matrix.addSubed(
                    Matrix.scaled(lowpass, filterCoefficient),
                    Matrix.scaled(accelerations, (1 - filterCoefficient)),
                    '+'
                )
            )
            highpass.copy(Matrix.addSubed(accelerations, lowpass, '-'))
            velocity = Matrix.addSubed(
                Matrix.scaled(
                    highpass,
                    0.5F * (timeStamp.second - timeStamp.first).toFloat()
                ), velocity, '+'
            )
            displacements = Matrix.addSubed(
                Matrix.scaled(
                    Matrix.addSubed(preVelocity, velocity, '+'),
                    0.5F * (timeStamp.second - timeStamp.first).toFloat()
                ), displacements, '+'
            )
            preVelocity = velocity
        }
    }

    fun getDisplacement(): Pair<FloatArray, FloatArray> {
        var temp = Matrix(3, 1)
        temp.copy(displacements)
        handler.post {
            displacements.zero()
        }
        var tempDisplacements: FloatArray = temp.toFloatArray()

        for (x in tempDisplacements.indices) {
            tempDisplacements[x] = tempDisplacements[x].toString().subSequence(0, 3).toString().toFloat().div(10)
        }

        Log.d(
            "SensorValue",
            "x:${tempDisplacements[0]}, y:${tempDisplacements[1]}, z:${tempDisplacements[2]}"
        )
        return Pair<FloatArray, FloatArray>(tempDisplacements, orientation)
    }

    fun reset() {
        accelerations = Matrix(3, 1)
        displacements = Matrix(3, 1)

        lowpass = Matrix(3, 1)
        highpass = Matrix(3, 1)
        preVelocity = Matrix(3, 1)
        velocity = Matrix(3, 1)
        timeStamp = Pair(0.0, 0.0)
    }
}
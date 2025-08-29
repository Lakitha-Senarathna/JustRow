package com.example.justrow;  // Replace with your package name

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class datascreen extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView tvTimeElapsed, tvDistance, tvCurrentSplit, tvAvgSplit;

    private long startTime;
    private long lastTime;
    private double velocity = 0.0;
    private double distance = 0.0;
    private float[] gravity = new float[3];
    private float[] linearAcc = new float[3];

    private static final float ALPHA = 0.8f;  // Low-pass filter constant
    private static final double SPLIT_DISTANCE = 500.0;  // Meters for split calculation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datascreen);

        tvTimeElapsed = findViewById(R.id.timeElapsed);
        tvDistance = findViewById(R.id.distance);
        tvCurrentSplit = findViewById(R.id.currentSplit);
        tvAvgSplit = findViewById(R.id.avgSplit);

        // Initialize sensor manager and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Reset variables
        startTime = System.currentTimeMillis();
        lastTime = startTime;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register sensor listener with normal delay
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            double dt = (currentTime - lastTime) / 1000.0;  // Time delta in seconds
            lastTime = currentTime;

            // low pass filter to isolate gravity
            gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * event.values[0];
            gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * event.values[1];
            gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * event.values[2];

            // high pass filter to get linear acceleration
            linearAcc[0] = event.values[0] - gravity[0];
            linearAcc[1] = event.values[1] - gravity[1];
            linearAcc[2] = event.values[2] - gravity[2];

            // Assume motion along x-axis; adjust axis as needed (e.g., use magnitude for simplicity but it may not capture direction)
            double accel = linearAcc[0];  // In m/s²

            // Integrate to get velocity and distance
            velocity += accel * dt;
            distance += velocity * dt;

            // Calculate metrics
            long timeElapsedMillis = currentTime - startTime;
            double timeElapsedSec = timeElapsedMillis / 1000.0;

            String currentSplitStr = "N/A";
            if (Math.abs(velocity) > 0.01) {  // Avoid division by zero or tiny values
                double currentSplit = SPLIT_DISTANCE / Math.abs(velocity);  // Seconds per 500m
                currentSplitStr = String.format("%.2f s/500m", currentSplit);
            }

            String avgSplitStr = "N/A";
            if (distance > 0.01) {
                double numSplits = distance / SPLIT_DISTANCE;
                double avgSplit = timeElapsedSec / numSplits;
                avgSplitStr = String.format("%.2f s/500m", avgSplit);
            }

            // Update UI
            tvTimeElapsed.setText(String.format("Time Elapsed: %.1fs", timeElapsedSec));
            tvDistance.setText(String.format("Distance: %.2fm", distance));
            tvCurrentSplit.setText("Current Split: " + currentSplitStr);
            tvAvgSplit.setText("Average Split: " + avgSplitStr);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used, but required by interface
    }
}
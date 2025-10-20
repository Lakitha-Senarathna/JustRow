package com.example.justrow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class justRow extends AppCompatActivity implements SensorEventListener {

    // UI
    private TextView tvTimeElapsed, tvDistance, tvCurrentSplit, tvAvgSplit, tvStrokeRate;
    private Button btnStart, btnStop, btnReset, btnSave;

    // Variable to save data to be stored in the database
    long saveTime = 0L;
    double saveAvgSplit = 0.0;
    double saveDistance = 0.0;

    // Metrics

    // GPS
    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;
    private double totalDistance = 0.0; // meters
    private long sessionStartTime = 0L;
    private boolean sessionRunning = false;

    private LocationCallback locationCallback;

    // Accelerometer
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float[] gravity = new float[3];
    private float[] linearAcc = new float[3];
    private static final float ALPHA = 0.8f;

    private final Deque<Long> strokeTimestamps = new ArrayDeque<>();
    private static final int STROKE_WINDOW = 6;
    private static final double SPLIT_DISTANCE = 500.0; // meters
    private static final double STROKE_THRESHOLD = 4.0; // m/s^2
    private static final long MIN_STROKE_GAP_MS = 800;

    private static final int REQ_PERM_LOCATION = 1001;

    private long lastStrokeDetectedAt = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.justrow);

        tvTimeElapsed = findViewById(R.id.timeElapsed);
        tvDistance = findViewById(R.id.distance);
        tvCurrentSplit = findViewById(R.id.currentSplit);
        tvAvgSplit = findViewById(R.id.avgSplit);
        tvStrokeRate = findViewById(R.id.strokeRate);

        btnStart = findViewById(R.id.btnStart);
        btnStop  = findViewById(R.id.btnStop);
        btnReset = findViewById(R.id.btnReset);
        btnSave  = findViewById(R.id.btnSave);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (accelerometer == null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (!sessionRunning) return;
                if (locationResult == null) return;
                for (Location loc : locationResult.getLocations()) {
                    handleNewLocation(loc);
                }
            }
        };

        btnStart.setOnClickListener(v -> startSession());
        btnStop.setOnClickListener(v -> stopSession());
        btnReset.setOnClickListener(v -> resetSession());
        btnSave.setOnClickListener(v ->saveSession());

        updateButtons();
        updateAllDisplays();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (sessionRunning && checkLocationPermission()) requestLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        if (!sessionRunning) fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        sensorManager.unregisterListener(this);
    }

    // Start of a session and control buttons
    private void startSession() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        if (!sessionRunning) {
            sessionRunning = true;
            sessionStartTime = System.currentTimeMillis();
            lastLocation = null;

            // Try to get last known location immediately
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    lastLocation = location;
                    updateMetricsFromLocation(location);
                } else {
                    tvDistance.setText("Waiting for GPS...");
                    tvCurrentSplit.setText("N/A");
                    tvAvgSplit.setText("N/A");
                }
            });

            requestLocationUpdates();
            updateButtons();
        }
    }

    private void stopSession() {
        if (sessionRunning) {
            sessionRunning = false;
            fusedLocationClient.removeLocationUpdates(locationCallback);
            updateButtons();

            // Getting elapsed time when session is stopped to be saved.
            long now = System.currentTimeMillis();
            double timeElapsedSec = (now - sessionStartTime) / 1000.0;

            // Getting average split data when session is stopped to be saved.
            double avgSplitSeconds = 0.0;
            if (totalDistance > 0) {
                double numSplits = totalDistance / SPLIT_DISTANCE;
                if (numSplits > 0) {
                    saveAvgSplit = timeElapsedSec / numSplits;
                }
            }
        }
    }

    private void resetSession() {
        stopSession();
        totalDistance = 0.0;
        strokeTimestamps.clear();
        sessionStartTime = 0L;
        lastLocation = null;
        updateAllDisplays();
    }

    private void saveSession() {
        double distance = totalDistance;
        long time = (long) saveTime;
        double averageSplit = saveAvgSplit;
    }

    private void updateButtons() {
        btnStart.setEnabled(!sessionRunning);
        btnStop.setEnabled(sessionRunning);
        btnReset.setEnabled(!sessionRunning);
        btnSave.setEnabled(!sessionRunning && (totalDistance > 10));
    }

    // Location data handling
    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERM_LOCATION);
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000 )
                .setMinUpdateIntervalMillis(500)
                .setMinUpdateDistanceMeters(1.0f)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void handleNewLocation(Location location) {
        if (lastLocation != null) {
            float segment = location.distanceTo(lastLocation);
            if (segment < 50.0) totalDistance += segment;
        }
        lastLocation = location;
        runOnUiThread(() -> updateMetricsFromLocation(location));
    }

    private void updateMetricsFromLocation(Location location) {
        long now = System.currentTimeMillis();
        double timeElapsedSec = sessionRunning ? (now - sessionStartTime) / 1000.0 : 0.0;

        tvTimeElapsed.setText(String.format("%.0f s", timeElapsedSec));

        if (lastLocation == null) {
            tvDistance.setText("Waiting for GPS...");
            tvCurrentSplit.setText("N/A");
            tvAvgSplit.setText("N/A");
        } else {
            tvDistance.setText(String.format("%.1f m", totalDistance));

            float speed = location.getSpeed();
            if (speed > 0.2f) {
                double currentSplit = SPLIT_DISTANCE / speed;

                // Formating current split data into minute : seconds format
                int minutes = (int) currentSplit / 60;
                int seconds = (int) currentSplit % 60;

                tvCurrentSplit.setText(String.format("%d:%d s/500m", minutes, seconds));
            } else {
                tvCurrentSplit.setText("N/A");
            }

            if (totalDistance > 10.0) {
                double numSplits = totalDistance / SPLIT_DISTANCE;
                double avgSplit = timeElapsedSec / numSplits;

                // Formating average split data into minute : seconds format
                int minutes = (int) avgSplit / 60;
                int seconds = (int) avgSplit % 60;

                tvAvgSplit.setText(String.format("%d:%d s/500m", minutes, seconds));
            } else {
                tvAvgSplit.setText("N/A");
            }
        }

        tvStrokeRate.setText(String.format("%d", computeRollingSPM()));
    }

    private void updateAllDisplays() {
        tvTimeElapsed.setText("0 s");
        tvDistance.setText("Waiting for GPS...");
        tvCurrentSplit.setText("N/A");
        tvAvgSplit.setText("N/A");
        tvStrokeRate.setText("0");
    }

    // Stroke detection
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!sessionRunning) return;

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            linearAcc[0] = event.values[0];
            linearAcc[1] = event.values[1];
            linearAcc[2] = event.values[2];
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            for (int i = 0; i < 3; i++) {
                gravity[i] = ALPHA * gravity[i] + (1 - ALPHA) * event.values[i];
                linearAcc[i] = event.values[i] - gravity[i];
            }
        } else {
            return;
        }

        double accelMag = Math.sqrt( linearAcc[0]*linearAcc[0] + linearAcc[1]*linearAcc[1] + linearAcc[2]*linearAcc[2]);

        long now = System.currentTimeMillis();

        if (accelMag > STROKE_THRESHOLD && (now - lastStrokeDetectedAt) > MIN_STROKE_GAP_MS) {
            lastStrokeDetectedAt = now;
            synchronized (strokeTimestamps) {
                strokeTimestamps.addLast(now);
                if (strokeTimestamps.size() > STROKE_WINDOW) {
                    strokeTimestamps.removeFirst();
                }
            }
            runOnUiThread(() -> tvStrokeRate.setText(String.format("%d", computeRollingSPM())));
        }
    }

    private int computeRollingSPM() {
        synchronized (strokeTimestamps) {
            if (strokeTimestamps.size() < 2) return 0;
            Iterator<Long> it = strokeTimestamps.iterator();
            long prev = it.next();
            double sumIntervalsMs = 0.0;
            int intervals = 0;
            while (it.hasNext()) {
                long cur = it.next();
                sumIntervalsMs += (cur - prev);
                prev = cur;
                intervals++;
            }
            if (intervals == 0) return 0;
            double avgIntervalSec = (sumIntervalsMs / intervals) / 1000.0;
            if (avgIntervalSec <= 0.0) return 0;
            return (int) Math.round(60.0 / avgIntervalSec);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERM_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (sessionRunning) requestLocationUpdates();
            } else {
                stopSession();
            }
        }
    }
}

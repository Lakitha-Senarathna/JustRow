package com.example.justrow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class customWorkout extends AppCompatActivity {

    TextView distance;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_workout);

        distance = findViewById(R.id.distance);
        startButton = findViewById(R.id.customSessionStart);

        String inputDistance = distance.getText().toString().trim();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (inputDistance.isEmpty()) {
                    Toast.makeText(customWorkout.this, "Distance Field can not be Empty",
                            Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(getApplicationContext(), customRow.class);
                intent.putExtra("distance", distance.getText().toString().trim());
                startActivity(intent);
                finish();

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(), dashboard.class);
        startActivity(intent);
        finish();
    }
}
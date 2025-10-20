package com.example.justrow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class saveSession extends AppCompatActivity {

    private EditText saveTitle;
    private Button saveButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_session);

        // Get current signed in user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();

        db = FirebaseFirestore.getInstance();

        saveTitle = findViewById(R.id.title);
        saveButton = findViewById(R.id.save_button);

        // Receiving data from justRow
        Intent intent = getIntent();

        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        double averageSplit = intent.getDoubleExtra("averageSplit", 0.0);
        double distance = intent.getDoubleExtra("distance", 0.0);
        long saveTime = intent.getLongExtra("savetime", 0);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Creating workout object to be saved to the database
                Map<String, Object> workout = new HashMap<>();
                workout.put("title", saveTitle.getText().toString());
                workout.put("saved_date", date);
                workout.put("saved_time", time);
                workout.put("distance", distance);
                workout.put("time", saveTime);
                workout.put("averageSplit", averageSplit);

                db.collection("Users").document(userID)
                        .collection("Workouts")
                        .add(workout)

                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(saveSession.this, "Workout Successfully Saved to DataBase",
                                        Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(getApplicationContext(), dashboard.class);
                                startActivity(intent);
                                finish();
                            }
                        })

                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(saveSession.this, "Error Occurred while Saving Workout, Try Again",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }
}
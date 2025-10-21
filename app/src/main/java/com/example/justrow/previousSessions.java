package com.example.justrow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.collections.ArrayDeque;

public class previousSessions extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> workoutTitles = new ArrayList<>();
    private List<Map<String, Object>> workoutDataList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.previous_sessions);

        listView = findViewById(R.id.listview);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, workoutTitles);
        listView.setAdapter(adapter);

        // Get current signed in user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String userID = user.getUid();

        db = FirebaseFirestore.getInstance();

        CollectionReference userWorkouts = db.collection("users")
                .document(userID)
                .collection("workouts");

        userWorkouts.orderBy("date", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    workoutTitles.clear();
                    for (DocumentSnapshot doc: queryDocumentSnapshots){
                        String title = doc.getString("title");

                        if (title != null){
                            workoutTitles.add(title);
                        }

                        Map<String, Object> workoutData = new HashMap<>();
                        if (doc.getData() != null) {
                            workoutData.putAll(doc.getData());
                        }

                        workoutDataList.add(workoutData);

                    }
                    adapter.notifyDataSetChanged();
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(previousSessions.this, "Error Occurred while Fetching Data",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                listView.setOnItemClickListener((parent, view, position, id) -> {

                    Map<String, Object> data = workoutDataList.get(position);

                    Intent intent = new Intent(getApplicationContext(), detailedView.class);
                    intent.putExtra("title", String.valueOf(data.get("title")));
                    intent.putExtra("distance", String.valueOf(data.get("distance")));
                    intent.putExtra("time", String.valueOf(data.get("time")));
                    intent.putExtra("averageSplit", String.valueOf(data.get("averageSplit")));
                    startActivity(intent);
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
package com.example.justrow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class detailedView extends AppCompatActivity {

    private TextView detailedViewTitle, detailedViewDistance, detailedViewTime, detailedViewAvgSplit;
    private Button shareWorkoutDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detailed_view);

        detailedViewTitle = findViewById(R.id.title);
        detailedViewDistance = findViewById(R.id.distance);
        detailedViewTime = findViewById(R.id.time);
        detailedViewAvgSplit = findViewById(R.id.avgSplit);
        shareWorkoutDetails = findViewById(R.id.shareButton);

        // Receiving data from previousSessions
        Intent intent = getIntent();

        String title = intent.getStringExtra("title");
        String distance = intent.getStringExtra("distance");
        String time = intent.getStringExtra("time");
        String averageSplit = intent.getStringExtra("averageSplit");

        Double distanceDouble = Double.parseDouble(distance);
        int distanceInt = (int) Math.round(distanceDouble);

        detailedViewTitle.setText(String.format("Workout Title : %s ", title));
        detailedViewDistance.setText(String.format("Distance : %s m", distanceInt));

        // Formatting time data into minute : seconds format
        if  (time != null) {
            double timeDouble = Double.parseDouble(time);
            int timeInt = (int) Math.round(timeDouble);

            if (timeInt > 60) {
                int minutes = timeInt / 60;
                int seconds = timeInt % 60;
                detailedViewTime.setText(String.format("Time : %d:%d", minutes, seconds));
            } else {
                detailedViewTime.setText(String.format("Time : %d s", timeInt));
            }
        }
        else {
            detailedViewTime.setText("Time : N/A");
        }

        // Formatting time AvgSplit into minute : seconds format
        if (averageSplit != null) {
            double avgSplitDouble = Double.parseDouble(averageSplit);
            int avgSplitInt = (int) Math.round(avgSplitDouble);

            if (avgSplitInt > 60) {
                int minutes = avgSplitInt / 60;
                int seconds = avgSplitInt % 60;
                detailedViewAvgSplit.setText(String.format("Average Split : %d:%d /500m", minutes, seconds));
            }
            else {
                detailedViewAvgSplit.setText(String.format("Average Split : %d s /500m", avgSplitInt));
            }
        }
        else {
            detailedViewAvgSplit.setText("Average Split : N/A");
        }

        shareWorkoutDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");

                // To create full, single text to be shared
                StringBuilder shareText = new StringBuilder();

                shareText.append(String.format("Workout Title : %s\n", title));

                Double distanceDouble = Double.parseDouble(distance);
                int distanceInt = (int) Math.round(distanceDouble);

                shareText.append(String.format("Distance : %s m\n", distanceInt));

                // Formating Time into minute : seconds format
                if  (time != null) {
                    double timeDouble = Double.parseDouble(time);
                    int timeInt = (int) Math.round(timeDouble);

                    if (timeInt >= 60) {
                        int minutes = timeInt / 60;
                        int seconds = timeInt % 60;
                        shareText.append(String.format("Time : %d:%02d s\n", minutes, seconds));
                    } else {
                        shareText.append(String.format("Time : %02d s\n", timeInt));
                    }
                }

                // Formating AvgSplit into minute : seconds format
                if (averageSplit != null) {
                    double avgSplitDouble = Double.parseDouble(averageSplit);
                    int avgSplitInt = (int) Math.round(avgSplitDouble);

                    if (avgSplitInt >= 60) {
                        int minutes = avgSplitInt / 60;
                        int seconds = avgSplitInt % 60;
                        shareText.append(String.format("Average Split : %d:%02d /500m\n", minutes, seconds));
                    }
                    else {
                        shareText.append(String.format("Average Split : %02d /500m\n", avgSplitInt));
                    }
                }
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
                startActivity(Intent.createChooser(shareIntent, "Share Workout Data"));
            }
        });
    }
}
package com.example.finalexamgilang;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

public class SummaryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView textViewSubjects, textViewTotalCredits;
    private String userId; // User ID from login or registration
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        textViewSubjects = findViewById(R.id.textViewSubjects);
        textViewTotalCredits = findViewById(R.id.textViewTotalCredits);

        // Assume userId is passed from the login or registration process
        userId = mAuth.getCurrentUser().getUid(); // Replace with actual userId from your auth system

        // Retrieve and display enrollment summary
        loadEnrollmentSummary();
    }

    private void loadEnrollmentSummary() {
        // Get the document reference from Firestore
        DocumentReference enrollmentRef = db.collection("enrollments").document(userId);

        enrollmentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();

                // Check if the document exists
                if (documentSnapshot.exists()) {
                    StringBuilder subjects = new StringBuilder();
                    int totalCredits = 0;

                    // Retrieve subjects and credits from Firestore
                    if (documentSnapshot.contains("subjects")) {
                        // Cast "subjects" field to a Map
                        Map<String, Object> subjectsMap = (Map<String, Object>) documentSnapshot.get("subjects");

                        // Iterate through the map
                        for (Map.Entry<String, Object> entry : subjectsMap.entrySet()) {
                            String subjectName = entry.getKey();
                            int credits = ((Long) entry.getValue()).intValue();

                            // Append subject name and credits to the display
                            subjects.append(subjectName).append("\n");
                            totalCredits += credits;
                        }
                    }

                    // Display the subjects and total credits
                    textViewSubjects.setText(subjects.toString());
                    textViewTotalCredits.setText("Total Credits: " + totalCredits);
                } else {
                    // Handle case where no enrollment data exists
                    textViewSubjects.setText("No subjects enrolled.");
                    textViewTotalCredits.setText("Total Credits: 0");
                }
            } else {
                // Handle failure to fetch data
                Toast.makeText(SummaryActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

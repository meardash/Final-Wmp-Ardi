package com.example.wmpardi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;

public class EnrollmentActivity extends AppCompatActivity {

    private LinearLayout checkBoxContainer; // Container to hold dynamically created checkboxes
    private Button buttonSubmitEnrollment;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private int totalCredits = 0;
    private HashMap<String, Integer> selectedSubjects = new HashMap<>();
    private HashMap<CheckBox, Integer> checkBoxData = new HashMap<>(); // Map to link checkboxes with their credit values

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        checkBoxContainer = findViewById(R.id.checkBoxContainer); // LinearLayout in XML to hold checkboxes
        buttonSubmitEnrollment = findViewById(R.id.buttonSubmitEnrollment);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        loadSubjectsFromFirestore(); // Fetch subjects from Firestore and populate checkboxes

        buttonSubmitEnrollment.setOnClickListener(v -> {
            selectedSubjects.clear();
            totalCredits = 0;

            // Iterate over all checkboxes to check which are selected
            for (CheckBox checkBox : checkBoxData.keySet()) {
                if (checkBox.isChecked()) {
                    String subject = checkBox.getText().toString();
                    int credits = checkBoxData.get(checkBox);
                    selectedSubjects.put(subject, credits);
                    totalCredits += credits;
                }
            }

            // Ensure total credits do not exceed 24
            if (totalCredits <= 24) {
                String userId = mAuth.getCurrentUser().getUid();

                // Create a Map for the data
                HashMap<String, Object> enrollmentData = new HashMap<>();
                enrollmentData.put("subjects", selectedSubjects);
                enrollmentData.put("totalCredits", totalCredits);

                // Save the enrollment data in Firestore under the user's document
                mFirestore.collection("enrollments")
                        .document(userId)
                        .set(enrollmentData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EnrollmentActivity.this, "Enrollment saved successfully!", Toast.LENGTH_SHORT).show();
                            // Optionally redirect to another activity
                            startActivity(new Intent(EnrollmentActivity.this, SummaryActivity.class));
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EnrollmentActivity.this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(EnrollmentActivity.this, "Total credits cannot exceed 24", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSubjectsFromFirestore() {
        // Fetch subjects from Firestore
        mFirestore.collection("subjects")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    checkBoxContainer.removeAllViews(); // Clear any existing checkboxes

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String subjectName = document.getString("subject");
                        String creditsString = document.getString("credits");

                        try {
                            int credits = Integer.parseInt(creditsString); // Convert credits to integer

                            // Create a new checkbox for each subject
                            CheckBox checkBox = new CheckBox(EnrollmentActivity.this);
                            checkBox.setText(String.format("%s (%s credits)", subjectName, creditsString));
                            checkBox.setTextColor(ContextCompat.getColor(EnrollmentActivity.this, R.color.defaultColor));

                            checkBoxContainer.addView(checkBox);

                            // Map checkbox to its credit value
                            checkBoxData.put(checkBox, credits);
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Invalid credits format for subject: " + subjectName, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EnrollmentActivity.this, "Error loading subjects: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Reset all the variables
        totalCredits = 0;

        // Uncheck all checkboxes
        for (CheckBox checkBox : checkBoxData.keySet()) {
            checkBox.setChecked(false);
        }
    }
}


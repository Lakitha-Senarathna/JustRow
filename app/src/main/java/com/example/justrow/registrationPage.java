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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class registrationPage extends AppCompatActivity {

    EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextConfirmPassword;
    Button regButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.registration_page);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextFirstName = findViewById(R.id.firstName);
        editTextLastName = findViewById(R.id.lastName);
        editTextEmail = findViewById(R.id.reg_email);
        editTextPassword = findViewById(R.id.reg_password);
        editTextConfirmPassword = findViewById(R.id.confirm_reg_password);
        //toLoginPage = findViewById(R.id);
        regButton = findViewById(R.id.reg_button);

        regButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String firstName, lastName, email, password, confirmPassword;
                firstName = editTextFirstName.getText().toString().trim();
                lastName = editTextLastName.getText().toString().trim();
                email = editTextEmail.getText().toString().trim();
                password = editTextPassword.getText().toString().trim();
                confirmPassword = editTextConfirmPassword.getText().toString().trim();

                if(!password.equals(confirmPassword)){
                    editTextPassword.setError("Passwords do not match");
                    editTextConfirmPassword.setError("Passwords do not match");
                }
                else if(firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
                    Toast.makeText(registrationPage.this, "All  input fields should not be empty.",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getInstance().getCurrentUser();

                                        Toast.makeText(registrationPage.this, "Account Creation Successful",
                                                Toast.LENGTH_SHORT).show();

                                        String userID = user.getUid();

                                        Map<String, Object> userInformation = new HashMap<>();
                                        userInformation.put("firstName", firstName);
                                        userInformation.put("lastName", lastName);
                                        userInformation.put("email", email);
                                        userInformation.put("registration_date", new Timestamp(new Date()));

                                        db.collection("Users").document(userID)
                                        .set(userInformation)

                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(registrationPage.this, "User Information Stored in the DataBase",
                                                                Toast.LENGTH_SHORT).show();

                                                        // Send user to login page
                                                        Intent intent = new Intent(getApplicationContext(), loginPage.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                })

                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(registrationPage.this, "Error Occurred while Saving Workout, Try Again",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });


                                    }
                                    else {
                                        // Exception handling
                                        Exception exception = task.getException();

                                        if(exception instanceof FirebaseAuthUserCollisionException){
                                            Toast.makeText(registrationPage.this, "User already exists",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            Toast.makeText(registrationPage.this, "Registration Failed",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }
}
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class registrationPage extends AppCompatActivity {

    EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextConfirmPassword;
    TextView toLoginPage;
    Button regButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.registration_page);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

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
                firstName = editTextFirstName.getText().toString();
                lastName = editTextLastName.getText().toString();
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();
                confirmPassword = editTextConfirmPassword.getText().toString();

                if(!password.equals(confirmPassword)){
                    editTextPassword.setError("Passwords do not match");
                    editTextConfirmPassword.setError("Passwords do not match");
                }
                else if(firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
                    Toast.makeText(registrationPage.this, "All field should not be empty.",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(registrationPage.this, "Authentication Successful.",
                                                Toast.LENGTH_SHORT).show();

                                        // Send user to login page
                                        Intent intent = new Intent(getApplicationContext(), loginPage.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(registrationPage.this, "Authentication Failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }
}
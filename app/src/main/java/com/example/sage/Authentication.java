package com.example.sage;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class Authentication extends AppCompatActivity {

    private EditText editTextEmail, editTextpassword;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        editTextEmail = findViewById(R.id.editTextEmailAddress);
        editTextpassword = findViewById(R.id.editTextpassword);
        progressBar = findViewById(R.id.progressBar2);

        authProfile = FirebaseAuth.getInstance();

        // Hide or Show password
        ImageView HideorShowpwd = findViewById(R.id.show_hide);
        HideorShowpwd.setImageResource(R.drawable.ic_hide_pwd);
        HideorShowpwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextpassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    // Password visible then Hiding it
                    editTextpassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //Changing the eye Icon
                    HideorShowpwd.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextpassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    HideorShowpwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        //Login Button
        Button LogIn = findViewById(R.id.LogIn);
        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String TextEmail = editTextEmail.getText().toString();
                String TextPwd = editTextpassword.getText().toString();

                if (TextUtils.isEmpty(TextEmail)) {
                    Toast.makeText(Authentication.this, "Enter your LogIN email address please", Toast.LENGTH_LONG).show();
                    editTextEmail.setError("Requires your email address!");
                    editTextEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(TextEmail).matches()) {
                    Toast.makeText(Authentication.this, "Please re-enter email!", Toast.LENGTH_LONG).show();
                    editTextEmail.setError("Please enter a valid email!");
                    editTextEmail.requestFocus();
                } else if (TextUtils.isEmpty(TextPwd)) {
                    Toast.makeText(Authentication.this, "Please enter your password!", Toast.LENGTH_LONG).show();
                    editTextpassword.setError("Password is Required!");
                    editTextpassword.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    LogIn(TextEmail, TextPwd);
                }
            }

            private void LogIn(String email, String pwd) {
                authProfile.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(Authentication.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Authentication.this, "Logged In successfully!", Toast.LENGTH_LONG).show();

                            //Get instance of the current user
                            FirebaseUser SageUser = authProfile.getCurrentUser();
                            //Checking if email is verified
                            if (SageUser.isEmailVerified()) {
                                Toast.makeText(Authentication.this, "Logged In successfully!", Toast.LENGTH_LONG).show();
                                //Open User Account Profile

                            } else {
                                SageUser.sendEmailVerification();
                                authProfile.signOut();//Signing out the User
                                alertDialogBox();
                            }
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                editTextEmail.setError("User account doesn't exist or is no more valid!");
                                editTextEmail.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                editTextEmail.setError("Invalid credentials please check and re-enter!");
                                editTextEmail.requestFocus();
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(Authentication.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            Toast.makeText(Authentication.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    private void alertDialogBox() {
                        //Setting up alert builder
                        AlertDialog.Builder builder = new AlertDialog.Builder(Authentication.this);
                        builder.setTitle("Email is NOT verified!");
                        builder.setMessage("Please verify the email address! You cannot login without email verification.");

                        //Opening the email app if the user clicks continue
                        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Opening Email app in new window
                                startActivity(intent);
                            }
                        });
                        // Creating the Alert Dialog Box
                        AlertDialog alertDialog = builder.create();
                        //Showing the alert Box
                        alertDialog.show();
                    }
                });
            }
        });
    }
    //Checking if the user is already logged in!
    //If they are logged in already take them to Sage's User page!

    @Override
    protected void onStart() {
        super.onStart();
        if (authProfile.getCurrentUser() != null) {
            Toast.makeText(Authentication.this, "You are already logged in!"
                    , Toast.LENGTH_SHORT).show();
            //Starting the user page!
            startActivity(new Intent(Authentication.this, UserPage.class));
            finish();// Closing the Authentication activity
        } else {
            Toast.makeText(Authentication.this, "Please log in!"
                    , Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackClick(View view) {
        // Navigate back to MainActivity
        onBackPressed();
    }

    public void onSignUpClick(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }
}

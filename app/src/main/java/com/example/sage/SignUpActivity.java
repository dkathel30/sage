package com.example.sage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private EditText FullName, EmailAddress, DateOfBirth,editTextPassword, editTextConfirmPassword;
    private RadioGroup Gender;
    private RadioButton RadioButtonSelected;

    private ProgressBar progressBar;

    private static final String TAG = "SignUpActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        progressBar = findViewById(R.id.progressBar);
        FullName = findViewById(R.id.FullName);
        EmailAddress = findViewById(R.id.EmailAddress);
        DateOfBirth = findViewById(R.id.EmailAddress);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);

        Gender = findViewById(R.id.Gender);
        Gender.clearCheck();

        Button SignUp = findViewById(R.id.Register);
        SignUp.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                int selectedGenderID = Gender.getCheckedRadioButtonId();
                RadioButtonSelected = findViewById(selectedGenderID);

                //Acquiring the data entered by user
                String fullname = FullName.getText().toString();
                String emailaddress = EmailAddress.getText().toString();
                String DOB = DateOfBirth.getText().toString();
                String password = editTextPassword.getText().toString();
                String confirmpassword = editTextConfirmPassword.getText().toString();
                String gender;

                if(TextUtils.isEmpty(fullname)){
                    Toast.makeText(SignUpActivity.this,"Enter your Fullname please", Toast.LENGTH_LONG).show();
                    FullName.setError("Requires your fullname!");
                    FullName.requestFocus();
                } else if (TextUtils.isEmpty(emailaddress)) {
                    Toast.makeText(SignUpActivity.this,"Enter your email address please", Toast.LENGTH_LONG).show();
                    EmailAddress.setError("Requires your email address");
                    EmailAddress.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailaddress).matches()) {
                    Toast.makeText(SignUpActivity.this,"Enter a valid email please", Toast.LENGTH_LONG).show();
                    EmailAddress.setError("Requires a valid email address!");
                    EmailAddress.requestFocus();
                } else if (TextUtils.isEmpty(DOB)) {
                    Toast.makeText(SignUpActivity.this,"Enter your date of birth please!", Toast.LENGTH_LONG).show();
                    EmailAddress.setError("Requires your date of birth");
                    EmailAddress.requestFocus();
                } else if (Gender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(SignUpActivity.this,"Please select a gender", Toast.LENGTH_LONG).show();
                    RadioButtonSelected.setError("Requires a valid email address!");
                    RadioButtonSelected.requestFocus();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(SignUpActivity.this,"Enter a password please!", Toast.LENGTH_LONG).show();
                    editTextPassword.setError("Please re-enter password!");
                    editTextPassword.requestFocus();
                } else if (password.length()<6) {
                    Toast.makeText(SignUpActivity.this,"Password must at-least be 6 digits!", Toast.LENGTH_LONG).show();
                    editTextPassword.setError("Password too weak");
                    editTextPassword.requestFocus();
                } else if (TextUtils.isEmpty(confirmpassword)) {
                    Toast.makeText(SignUpActivity.this,"Enter the confirm password please!", Toast.LENGTH_LONG).show();
                    editTextConfirmPassword.setError("Please re-enter confirm password!");
                    editTextConfirmPassword.requestFocus();
                } else if (!password.equals(confirmpassword)) {
                    Toast.makeText(SignUpActivity.this,"Make sure to enter the same password", Toast.LENGTH_LONG).show();
                    editTextConfirmPassword.setError("Confirmation of password is required!");
                    editTextConfirmPassword.requestFocus();
                    editTextPassword.clearComposingText();
                    editTextConfirmPassword.clearComposingText();
                }else {
                    gender = RadioButtonSelected.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    SignUpUser(fullname,emailaddress,DOB,gender,password);

                }
            }
        });
    }
    private void SignUpUser(String fullname, String emailaddress, String DOB, String gender,String password){
        FirebaseAuth authentication = FirebaseAuth.getInstance();
        authentication.createUserWithEmailAndPassword(emailaddress,password).addOnCompleteListener(SignUpActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SignUpActivity.this,"Account has been created", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE)
                            FirebaseUser SageUser = authentication.getCurrentUser();

                            //Send Firebase Verfication Email
                            SageUser.sendEmailVerification();

                            /*//Open User Account with Sage
                            Intent intent = new Intent(SignUpActivity.this, UserAccount.class);

                            //Preventing User from returning to SignUp Activity on pressing GoBackButton
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            //Ending the activity
                            finish();*/

                        }else {
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                editTextPassword.setError("The email address provide is already in use or invalid! Kindly re-enter.");
                                editTextPassword.requestFocus();
                            } catch(FirebaseAuthUserCollisionException e) {
                                editTextPassword.setError("The Email Address is already associated with a Sage user account!");
                                editTextPassword.requestFocus();
                            } catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(SignUpActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }
    public void onBackClick(View view){
        onBackPressed();
    }

}
package com.example.justintimecycling.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.justintimecycling.R;
import com.example.justintimecycling.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText rName, rPass, rEmail, rPassRep;
    private TextView loginBtn;
    private Button RegisterBtn;
    private FirebaseAuth fAuth;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        rName = findViewById(R.id.regName);
        rEmail = findViewById(R.id.regEmail);
        rPass = findViewById(R.id.regPassword);
        rPassRep = findViewById(R.id.regPassRepeat);
        loginBtn = findViewById(R.id.txtLogin);
        RegisterBtn = findViewById(R.id.BtnReg);
        progressBar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        hideSoftKeyboard();

        if(fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }

        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = rEmail.getText().toString().trim();
                String password = rPass.getText().toString().trim();
                String passwordRepeat = rPassRep.getText().toString().trim();

                //Verify input
                if(TextUtils.isEmpty(email)) {
                    rEmail.setError("Email is required");
                    return;
                }
                if(TextUtils.isEmpty(password)) {
                    rPass.setError("Password is required");
                    return;
                }
                if(password.length() < 8) {
                    rPass.setError("Password must be 8 characters long");
                    return;
                }
                if(!password.equals(passwordRepeat)) {
                    rPassRep.setError("Passwords don't match");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                //Register the user data
                fAuth.createUserWithEmailAndPassword(email, password). addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "onComplete: AuthState: " + FirebaseAuth.getInstance().getCurrentUser().getUid());

                            //insert some default data
                            User user = new User();
                            user.setEmail(email);
                            user.setUsername(email.substring(0, email.indexOf("@")));
                            user.setUser_id(FirebaseAuth.getInstance().getUid());

                            DocumentReference newUserRef = db
                                    .collection(getString(R.string.collection_users))
                                    .document(FirebaseAuth.getInstance().getUid());

                            newUserRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(RegisterActivity.this, "User Created", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                        finish();
                                    }else{
                                        View parentLayout = findViewById(android.R.id.content);
                                        Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error!"+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                    }
                });


            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
package com.example.billifyfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Register extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference reference;
    String username,phone,password,usertype;
    EditText registerPhonenumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        TextView LoginBtn = findViewById(R.id.login_btn);
        ImageView RegisterBtn = findViewById(R.id.register_btn);

        EditText registerUsername = findViewById(R.id.username);
        registerPhonenumber = findViewById(R.id.phonenumber);
        EditText registerPassword = findViewById(R.id.password);


        //Redirect to Login Page
        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
            }
        });

        //Redirect to Home Page
        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //data base connection & initiallization
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                //radio button selection
                username = registerUsername.getText().toString();
                 phone = registerPhonenumber.getText().toString();
                 password = registerPassword.getText().toString();

                RadioGroup rbutton = findViewById(R.id.group);
                int rid = rbutton.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(rid);
                 usertype = radioButton.getText().toString();
                //Cheching username

                reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Username already exists
                            registerUsername.setError("Username already exists! Please choose another one.");
                        } else {
                            addUser();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Toast.makeText(Register.this, "Database Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }

        });
    }
    private void addUser(){
        if (usertype == null) {
            Toast.makeText(Register.this, "Select the user type", Toast.LENGTH_SHORT).show();
        } else if (!username.isEmpty() && !phone.isEmpty() && !password.isEmpty() ) {
            if( phone.length() != 10){
                registerPhonenumber.setError("Enter correct phone number");
            }
            else {

                // Proceed with registration
                HelperClass helperClass = new HelperClass(username, phone, password, usertype);
                reference.child(username).setValue(helperClass);

                //sending response to user and redirect
                Toast.makeText(Register.this, "You have signed up successfully!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Register.this, Login.class);
                startActivity(i);
                finish();
            }
        } else {
            // Show error message
            Toast.makeText(Register.this, "Please enter all required data!", Toast.LENGTH_LONG).show();
        }
    }
}
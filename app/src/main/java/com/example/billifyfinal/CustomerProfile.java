package com.example.billifyfinal;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

public class CustomerProfile extends AppCompatActivity {
    EditText name , phone , email, address ;
    String userNameFromIntent , nametext , phoneText , emailText , addText;
    TextView UsernameID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        userNameFromIntent = getIntent().getStringExtra("uname");
        Button save =findViewById(R.id.saveCusstomerData);
        UsernameID = findViewById(R.id.username);
        name = findViewById(R.id.fullname);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        address = findViewById(R.id.address);



        ImageView goBack = findViewById(R.id.back_button);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        dataFromDB();



        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nametext = name.getText().toString();
                emailText = email.getText().toString();
                phoneText = phone.getText().toString();
                addText = address.getText().toString();
                Log.d("tag", "onClick: " + nametext + emailText + phoneText +addText);
                if (nametext.equals("") && emailText.equals("") && phoneText.equals("") && addText.equals("")) {
                    Toast.makeText(CustomerProfile.this, "Please, fill details", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(userNameFromIntent);
                    usersRef.child("name").setValue(name.getText().toString());
                    usersRef.child("email").setValue(email.getText().toString());
                    usersRef.child("address").setValue(address.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(CustomerProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CustomerProfile.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }
        });

    }
    private void dataFromDB(){
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        Query query = usersRef.orderByChild("username").equalTo(userNameFromIntent);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Retrieve user data
                    String usernameDB = userSnapshot.child("username").getValue(String.class);
                    String phoneDB = userSnapshot.child("phone").getValue(String.class);
                    String nameDB = userSnapshot.child("name").getValue(String.class);
                    String emailDB = userSnapshot.child("email").getValue(String.class);
                    String addDB = userSnapshot.child("address").getValue(String.class);

                    Log.d("tag", "data: " + usernameDB + phoneDB + nameDB + emailDB + addDB);

                    // Process the retrieved user data

                    UsernameID.setText("@"+usernameDB);
                    name.setText(nameDB);
                    phone.setText(phoneDB);
                    email.setText(emailDB);
                    address.setText(addDB);

                    if (name.getText().toString().isEmpty()){
                        name.setEnabled(true);
                    } else{
                        name.setEnabled(false);
                    }

                    if (phone.getText().toString().isEmpty()){
                        phone.setEnabled(true);
                    } else{
                        phone.setEnabled(false);
                    }

                    if (email.getText().toString().isEmpty()){
                        email.setEnabled(true);
                    } else{
                        email.setEnabled(false);
                    }

                    if (address.getText().toString().isEmpty()){
                        address.setEnabled(true);
                    } else{
                        address.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                Log.e("Firebase", "Error getting data: " + databaseError.getMessage());
            }
        });

    }
}
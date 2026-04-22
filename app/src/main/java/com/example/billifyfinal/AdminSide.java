package com.example.billifyfinal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminSide extends AppCompatActivity {
    EditText itemId , itemName , itemPrice , itemDec ;
    FirebaseDatabase database;
    DatabaseReference reference;
    Button addItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_side);

        String itemIDFromIntent = getIntent().getStringExtra("itemID");
        ImageView backBtn = findViewById(R.id.back_button);
        itemId = findViewById(R.id.item_id);
        itemName = findViewById(R.id.item_name);
        itemPrice = findViewById(R.id.item_price);
        itemDec = findViewById(R.id.item_dec);
        addItem = findViewById(R.id.item_add);
            if (itemIDFromIntent != null){
                itemId.setText(itemIDFromIntent);
                itemId.setEnabled(false);
            }

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("items");

                String id = itemId.getText().toString();
                String name = itemName.getText().toString();
                String price = itemPrice.getText().toString();
                String dec = itemDec.getText().toString();
                if(id.equals(null) && name.equals(null) && price.equals(null) && dec.equals(null)){
                    Toast.makeText(AdminSide.this, "Enter the Data", Toast.LENGTH_SHORT).show();
                }
                else{

                ItemHelperclass helperClass = new ItemHelperclass(id, name, price, dec);
                reference.child(id).setValue(helperClass, new DatabaseReference.CompletionListener() {

                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error != null) {
                            // Error occurred while writing data
                            Toast.makeText(AdminSide.this, "Failed to add item: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            // Data was successfully written to Firebase
                            Toast.makeText(AdminSide.this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                            clearFields(); // Clear input fields
                        }
                    }
                });
                }
            }
        });
    }
    // Clear input fields
    private void clearFields() {
        itemId.setText("");
        itemName.setText("");
        itemPrice.setText("");
        itemDec.setText("");
    }
}


package com.example.billifyfinal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AdminHome extends AppCompatActivity {
    LinearLayout containerLayout , itemLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
    containerLayout = findViewById(R.id.containerLayout);
    itemLayout = findViewById(R.id.itemLayout);
        ImageView userRefBtn = findViewById(R.id.userRef);
        ImageView itemRef = findViewById(R.id.itemRef);
        ImageView RemoveData = findViewById(R.id.RemoveData);

        shopkeeperName();

        itemData();

        userRefBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                containerLayout.removeAllViews();
                shopkeeperName();
            }
        });
        itemRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemLayout.removeAllViews();
                itemData();
            }
        });
        RemoveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminHome.this);
                builder.setTitle("Billify Warnnig !!")
                        .setMessage("Remove Pandding Bills ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeData();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do something when "No" button is clicked
                                // For example, dismiss the dialog
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

    }
    private void removeData() {

        DatabaseReference billsRef = FirebaseDatabase.getInstance().getReference("Bills");
        billsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through the children nodes of the "Bills" node
                for (DataSnapshot billIdSnapshot : dataSnapshot.getChildren()) {
                    String status = billIdSnapshot.child("status").getValue(String.class);
                    if (status == null ) {
                        String billKey = billIdSnapshot.getKey();
                        if (billKey != null) {
                            // Remove the child node
                            billsRef.child(billKey).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Node and its children successfully deleted
                                            Toast.makeText(AdminHome.this, "Bills deleted successfully", Toast.LENGTH_SHORT).show();
                                            Log.d("TAG", "Node deleted successfully");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle any error
                                            Toast.makeText(AdminHome.this, "Deletion failed", Toast.LENGTH_SHORT).show();
                                            Log.w("TAG", "Error deleting node", e);
                                        }
                                    });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

            private void itemData(){
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("items");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    String itemName = itemSnapshot.child("itemName").getValue(String.class);
                    String itemId = itemSnapshot.child("itemId").getValue(String.class);
                    String itemPrice = itemSnapshot.child("itemPrice").getValue(String.class);
                    String itemDec = itemSnapshot.child("itemDec").getValue(String.class);

                    PopulateData(itemName , itemId , itemDec , itemPrice);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                Log.e("Firebase", "Error getting data: " + databaseError.getMessage());
            }
        });
    }

    private  void PopulateData(String Name ,String ID , String Dec , String Price){
        View billItemView = LayoutInflater.from(this).inflate(R.layout.bill_item, null);
        TextView shopkeeperTextView = billItemView.findViewById(R.id.shopkeeper_textview);
        TextView dateTextView = billItemView.findViewById(R.id.date_textview);
        TextView totalTextView = billItemView.findViewById(R.id.total_textview);
        TextView billIdFromView = billItemView.findViewById(R.id.billId);
        TextView totalText = billItemView.findViewById(R.id.totalText);

        shopkeeperTextView.setText(Name);
        dateTextView.setText("ID:  " +ID);
        totalTextView.setText("₹ " + Price);
        billIdFromView.setText(Dec);
        totalText.setText("Price");
        // Add bill item to history container
        itemLayout.addView(billItemView);
    }
    private void shopkeeperName(){

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Query query = usersRef.orderByChild("usertype").equalTo("Shopkeeper");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String username = userSnapshot.child("username").getValue(String.class);

                    addToUI(username);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
                Log.e("Firebase", "Error getting data: " + databaseError.getMessage());
            }
        });
    }
    private void addToUI(String username){
        View userView = LayoutInflater.from(this).inflate(R.layout.user_admin, null);
        TextView textView = userView.findViewById(R.id.shopkeeperName);
        textView.setText(username);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 16); // Add 16dp bottom margin between each user view
        userView.setLayoutParams(layoutParams);

        userView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminHome.this, HistoryPage.class);
                intent.putExtra("shopID", username);
                startActivity(intent);
            }
        });
        containerLayout.addView(userView);
    }
}
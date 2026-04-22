package com.example.billifyfinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HistoryPage extends AppCompatActivity {
    private LinearLayout historyContainer;
    private String shopkeeperID, billId;

    DatabaseReference billsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_page);
        historyContainer = findViewById(R.id.history_container);
        shopkeeperID = getIntent().getStringExtra("shopID");
       ImageView back_btn = findViewById(R.id.backBtn);

       back_btn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               finish();
           }
       });

        billsRef = FirebaseDatabase.getInstance().getReference("Bills");
        HistoryCollect();

        TextView ref_btn = findViewById(R.id.refresh_btn);

        ref_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyContainer.removeAllViews();
                HistoryCollect();
            }
        });

    }
    private void HistoryCollect(){
        // Add a ValueEventListener to listen for data changes
        billsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through the children nodes of the "Bills" node
                for (DataSnapshot billIdSnapshot : dataSnapshot.getChildren()) {
                    // Check if the current billId contains the given customer username
                    String shopkeeper = billIdSnapshot.child("shopkeeperUsername").getValue(String.class);
                    Log.d("tag", "History : " + shopkeeper +shopkeeperID);
                    if (shopkeeper != null && shopkeeper.equals(shopkeeperID)){
                        String status = billIdSnapshot.child("status").getValue(String.class);
                        if (status != null && status.equals("Checked")) {

                            billId = billIdSnapshot.child("billId").getValue(String.class);
                            Log.d("tag", "onDataChange: "+ billId);
                            String customerUsername = billIdSnapshot.child("customerUsername").getValue(String.class); // corrected field name
                            String Date = billIdSnapshot.child("date").getValue(String.class);
                            Double Total = billIdSnapshot.child("grandTotal").getValue(Double.class);
                            Log.d("tag", "onDataChange: " + customerUsername + Date + Total +billId);

                            addBillToHistory(customerUsername, Date, Total ,billId);
                        }
                    }
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e("Firebase", "Error getting data: " + databaseError.getMessage());
            }
        });
    }
    private void addBillToHistory(String customerUsername, String date, Double grandTotal, String BillId) {
        // Inflate bill item layout and populate with details
        View billItemView = LayoutInflater.from(this).inflate(R.layout.bill_item, null);
        TextView shopkeeperTextView = billItemView.findViewById(R.id.shopkeeper_textview);
        TextView dateTextView = billItemView.findViewById(R.id.date_textview);
        TextView totalTextView = billItemView.findViewById(R.id.total_textview);
        TextView billIdFromView = billItemView.findViewById(R.id.billId);
        shopkeeperTextView.setText(customerUsername);
        dateTextView.setText(date);
        totalTextView.setText("₹ " + String.valueOf(grandTotal));
        billIdFromView.setText(BillId);
        billItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryPage.this, GeneratedBill.class);
                intent.putExtra("bill_id", billIdFromView.getText());
                startActivity(intent);
            }
        });
        // Add bill item to history container
        historyContainer.addView(billItemView);
    }
}

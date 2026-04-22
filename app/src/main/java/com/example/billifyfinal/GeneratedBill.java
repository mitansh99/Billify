package com.example.billifyfinal;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.google.firebase.database.ValueEventListener;

public class GeneratedBill extends AppCompatActivity {

    private static final String TAG = "GeneratedBill";
    private double subTotal;
    private DatabaseReference billsRef;
    private DatabaseReference shopkeepersRef;
    private DatabaseReference customersRef;
    private DatabaseReference itemsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generated_bill);


        String billId = getIntent().getStringExtra("bill_id");
        String vaildUser = getIntent().getStringExtra("vaildUser");
        Button sendToUSer = findViewById(R.id.send_btn);
        if (vaildUser != null) {
            sendToUSer.setVisibility(View.VISIBLE);
        }
        ImageView backbutton = findViewById(R.id.back_button);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        billsRef = database.getReference("Bills");

        DatabaseReference singleBillRef = billsRef.child(billId);

        sendToUSer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                singleBillRef.child("status").setValue("Checked")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // The value was successfully set
                                Toast.makeText(GeneratedBill.this, "sent successfully!!", Toast.LENGTH_SHORT).show();
                                sendToUSer.setVisibility(View.GONE);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // The value set operation failed
                                Toast.makeText(GeneratedBill.this, "sent Failed!!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        singleBillRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    String shopkeeperUID = dataSnapshot.child("shopkeeperUsername").getValue(String.class);
                    String customerUID = dataSnapshot.child("customerUsername").getValue(String.class);
                    String date = dataSnapshot.child("date").getValue(String.class);
                    String invoiceNumber = dataSnapshot.child("billId").getValue(String.class);

                    shopkeepersRef = database.getReference("users").child(shopkeeperUID);
                    shopkeepersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot shopkeeperSnapshot) {
                            try {
                                String shopkeeperName = shopkeeperSnapshot.child("username").getValue(String.class);
                                String shopkeeperContact = shopkeeperSnapshot.child("phone").getValue(String.class);

                                customersRef = database.getReference("users").child(customerUID);
                                customersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot customerSnapshot) {
                                        try {
                                            String customerName = customerSnapshot.child("username").getValue(String.class);
                                            String customerContact = customerSnapshot.child("phone").getValue(String.class);

                                            TextView shopkeeperNameTextView = findViewById(R.id.shopkeeper_name);
                                            shopkeeperNameTextView.setText("Name : @" + shopkeeperName);

                                            TextView shopkeeperNumberTextView = findViewById(R.id.shopkeeper_number);
                                            shopkeeperNumberTextView.setText("Contact : +91 " + shopkeeperContact);

                                            TextView customerNameTextView = findViewById(R.id.customer_name);
                                            customerNameTextView.setText("Name : @" + customerName);

                                            TextView customerNumberTextView = findViewById(R.id.customer_phone);
                                            customerNumberTextView.setText("Contact : +91 " + customerContact);

                                            TextView billIdentification = findViewById(R.id.invoice_number);
                                            billIdentification.setText("Invoice number : \n" + "  " + invoiceNumber);

                                            TextView billDate = findViewById(R.id.invoice_date);
                                            billDate.setText("Date : " + date);

                                            TableLayout tableLayout = findViewById(R.id.table_layout);
                                            for (DataSnapshot itemSnapshot : dataSnapshot.child("items").getChildren()) {
                                                String itemID = itemSnapshot.getKey();
                                                String quantity = itemSnapshot.child("item_quantity").getValue(String.class);
                                                itemsRef = database.getReference("items").child(itemID);
                                                itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot itemSnapshot) {
                                                        try {
                                                            String itemName = itemSnapshot.child("itemName").getValue(String.class);
                                                            String itemPrice = itemSnapshot.child("itemPrice").getValue(String.class);

                                                            double total = Integer.parseInt(quantity) * Double.parseDouble(itemPrice);
                                                            String totalString = String.valueOf(total);

                                                            subTotal += total;

                                                            TableRow row = new TableRow(GeneratedBill.this);
                                                            TextView nameTextView = new TextView(GeneratedBill.this);
                                                            nameTextView.setText(itemName);
                                                            nameTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                                                            nameTextView.setGravity(Gravity.CENTER);

                                                            TextView qtyTextView = new TextView(GeneratedBill.this);
                                                            qtyTextView.setText(quantity);
                                                            qtyTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                                                            qtyTextView.setGravity(Gravity.CENTER);

                                                            TextView priceTextView = new TextView(GeneratedBill.this);
                                                            priceTextView.setText(itemPrice);
                                                            priceTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                                                            priceTextView.setGravity(Gravity.CENTER);

                                                            TextView totalPrice = new TextView(GeneratedBill.this);
                                                            totalPrice.setText(totalString);
                                                            totalPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
                                                            totalPrice.setGravity(Gravity.CENTER);

                                                            row.addView(nameTextView);
                                                            row.addView(qtyTextView);
                                                            row.addView(priceTextView);
                                                            row.addView(totalPrice);
                                                            row.setPadding(0, 7, 0, 7);
                                                            tableLayout.addView(row);


                                                            TextView subtotalTextView = findViewById(R.id.subtotal);
                                                            subtotalTextView.setText(String.valueOf("Sub Total : " + " ₹ " + subTotal));
                                                            double taxRate = 0.01;
                                                            double taxAmount = subTotal * taxRate;

                                                            // Add tax amount to the overall total
                                                            double totalWithTax = subTotal + taxAmount;
                                                            //Tota lis upadted in db
                                                            singleBillRef.child("grandTotal").setValue(totalWithTax);

                                                            TextView tax = findViewById(R.id.tax);
                                                            tax.setText("Tax : " + taxRate + " %");
                                                            TextView grandTotal = findViewById(R.id.grand_total);
                                                            grandTotal.setText(String.valueOf("Total : " + " ₹ " + totalWithTax));


                                                        } catch (Exception e) {
                                                            Log.e(TAG, "Error fetching item data: " + e.getMessage());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        Log.e(TAG, "Database error: " + databaseError.getMessage());
                                                    }
                                                });
                                            }

                                        } catch (Exception e) {
                                            Log.e(TAG, "Error fetching customer data: " + e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e(TAG, "Database error: " + databaseError.getMessage());
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "Error fetching shopkeeper data: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, "Database error: " + databaseError.getMessage());
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error fetching bill data: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

}

package com.example.billifyfinal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScanBarcode extends AppCompatActivity {
    private CodeScanner codeScanner;
    private TextView codeData, scannedItemName, scannedItemPrice, scannedItemDec, cross;
    private CodeScannerView scannerView;
    private Button toggleButton, saveUsername ,saveItemBtn;
    private EditText quantity, customerUsername;
    private boolean isScanning = true , DataAdded = false;
    private LinearLayout dataPop, userPop;

    public String itemID, uniqueID, ItemCode;
    FirebaseDatabase database;
    DatabaseReference reference;
    String ShopKeeperProfile;
    private static final String KEY_COUNT = "count";
    private int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        ShopKeeperProfile = getIntent().getStringExtra("username");


        codeData = findViewById(R.id.text_code);
        scannerView = findViewById(R.id.scanner_view);
        toggleButton = findViewById(R.id.toggle_button);
        scannedItemName = findViewById(R.id.scanned_item_name);
        scannedItemPrice = findViewById(R.id.scanned_item_price);
        scannedItemDec = findViewById(R.id.scanned_item_dec);
        quantity = findViewById(R.id.quantity);
        cross = findViewById(R.id.cross);
        customerUsername = findViewById(R.id.customer_username);
        saveUsername = findViewById(R.id.save_username);
        dataPop = findViewById(R.id.data_pop);
        userPop = findViewById(R.id.username_pop);
        saveItemBtn = findViewById(R.id.save_item);

        codeData.setText("Enter Username");
        toggleButton.setVisibility(View.GONE);
        saveItemBtn.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            // Restore the saved state

            count = savedInstanceState.getInt(KEY_COUNT, 0);
        }


        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataAdded) {

                    stopScanning();
                    AlertDialog.Builder builder = new AlertDialog.Builder(ScanBarcode.this);
                    builder.setTitle("Billify")
                            .setMessage("Do you want to genrate a bill.")
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                startScanning();
                                dialog.dismiss();
                                }
                            })
                            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(ScanBarcode.this, GeneratedBill.class);
                                    i.putExtra("bill_id" , uniqueID);
                                    i.putExtra("vaildUser",ShopKeeperProfile);
                                    startActivity(i);
                                    finish();
                                }
                            })
                            .show();
                }
                else{
                    Toast.makeText(ScanBarcode.this, "Please Add item", Toast.LENGTH_SHORT).show();
                }
            }
        });
        saveItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItems();
            }
        });
        runCodeScanner();
    }


    //All methods created for pertticuler work
    private void runCodeScanner() {
        codeScanner = new CodeScanner(this, scannerView);
        saveUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (customerUsername.getText().toString().isEmpty()) {
                    Toast.makeText(ScanBarcode.this, "Enter Customer Username", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
                    Query checkUserDatabase = reference.orderByChild("username").equalTo(customerUsername.getText().toString());
                    checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                customerUsername.setError(null);
                                String userTypeFromDB = snapshot.child(customerUsername.getText().toString()).child("usertype").getValue(String.class);
                                if (userTypeFromDB.equals("Customer")) {
                                    saveBillData();
                                    toggleButton.setVisibility(View.VISIBLE);
                                    saveItemBtn.setVisibility(View.VISIBLE);
                                    codeData.setText("scannig...");
                                    codeScanner.startPreview();
                                    userPop.setVisibility(View.GONE);
                                    dataPop.setVisibility(View.VISIBLE);
                                } else {
                                    customerUsername.setError("Customer does not exists");
                                }
                            } else {
                                customerUsername.setError("Username does not existes");
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
        codeScanner.setAutoFocusEnabled(true);
        codeScanner.setFormats(CodeScanner.ALL_FORMATS);
        codeScanner.setScanMode(ScanMode.CONTINUOUS);
        codeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = result.getText();
                        itemID = data;
                        codeData.setText(data);
                        stopScanning(); // Stop scanning after one output
                        checkItem();
                    }
                });
            }
        });
    }

    public void checkItem() {
        ItemCode = itemID.toString().trim();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("items");
        Query checkItemDatabase = reference.orderByChild("itemId").equalTo(ItemCode);
        checkItemDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    codeData.setText("success !!");
                    String nameFromDB = snapshot.child(ItemCode).child("itemName").getValue(String.class);
                    String priceFromDB = snapshot.child(ItemCode).child("itemPrice").getValue(String.class);
                    String decFromDB = snapshot.child(ItemCode).child("itemDec").getValue(String.class);


                    scannedItemName.setText(nameFromDB);
                    scannedItemPrice.setText("₹ " + priceFromDB);
                    scannedItemDec.setText(decFromDB);
                    cross.setVisibility(View.VISIBLE);
                    quantity.setVisibility(View.VISIBLE);

                } else {
                    showAlertDialog();
                    codeData.setText("item does not exit");
                    codeData.requestFocus();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startScanning() {
        clean();
        codeScanner.startPreview();
        isScanning = true;
    }


    private void stopScanning() {
        codeScanner.stopPreview();
        isScanning = false;
    }

    private void clean() {
        scannedItemName.setText("");
        scannedItemPrice.setText("");
        scannedItemDec.setText("");
        cross.setVisibility(View.GONE);
        quantity.setText("");
        quantity.setVisibility(View.GONE);
        codeData.setText("Scanning...");
    }

    private void saveBillData() {
        //gettign cureent date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        String currentDate = dateFormat.format(calendar.getTime());
        //Genrating Unique id
        uniqueID = UUID.randomUUID().toString();
        //connect with data Base
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Bills");
        //Add data into DB
        BillHelperClass billHelperClass = new BillHelperClass(uniqueID, ShopKeeperProfile, customerUsername.getText().toString(), currentDate);
        reference.child(uniqueID).setValue(billHelperClass);
    }

    private void addItems() {
        if (quantity.getText().toString().isEmpty()) {
            quantity.setError("Enter Item Quantity");
        } else {


                    String itemId = reference.child(uniqueID).child("items").push().getKey();
                    String itemIdPath = "Bills/" + uniqueID + "/items/" + ItemCode;
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("item_id", ItemCode);
                    itemData.put("item_quantity", quantity.getText().toString());

                    // Update the database with the new item
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put(itemIdPath, itemData);
                    database.getReference().updateChildren(childUpdates)
                            .addOnSuccessListener(aVoid -> {
                                clean();
                                Toast.makeText(ScanBarcode.this, "item added", Toast.LENGTH_SHORT).show();
                                startScanning();
                                DataAdded = true;
                            })
                            .addOnFailureListener(e -> { Toast.makeText(ScanBarcode.this, "item is not added", Toast.LENGTH_SHORT).show();});


                }

               
    }
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Billify")
                .setMessage("Do you want to add item.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle positive button click
                        Intent additem = new Intent(ScanBarcode.this, AdminSide.class);
                        additem.putExtra("itemID" , itemID);
                        startActivity(additem);
                        codeData.setText("scanning ...");
                        startScanning();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle negative button click
                        startScanning();
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the state of your activity
        outState.putInt(KEY_COUNT, count);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the saved state
        count = savedInstanceState.getInt(KEY_COUNT, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save the state of your activity when it is paused
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_COUNT, count);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restore the saved state when the activity is resumed
        SharedPreferences preferences = getPreferences(Context.MODE_PRIVATE);
        count = preferences.getInt(KEY_COUNT, 0);
    }

    // Example method to increment count
    public void incrementCount(View view) {
        count++;
    }
}

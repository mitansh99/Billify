package com.example.billifyfinal;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    private Button toggleButton, saveUsername ,saveItemBtn, manualEntryBtn, searchManualBtn, cancelItemBtn;
    private EditText quantity, customerUsername;
    private AutoCompleteTextView manualItemId;
    private boolean isScanning = true , DataAdded = false;
    private LinearLayout dataPop, userPop, manualPop;
    private ScrollView scrollContainer;

    private Handler debounceHandler = new Handler();
    private Runnable debounceRunnable;
    private ArrayAdapter<String> adapter;
    private Map<String, String> itemNameToIdMap = new HashMap<>();

    public String itemID, uniqueID, ItemCode;
    FirebaseDatabase database;
    DatabaseReference reference;
    String ShopKeeperProfile;
    private static final String KEY_COUNT = "count";
    private int count = 0;
    private static final int CAMERA_PERMISSION_CODE = 101;

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
        manualPop = findViewById(R.id.manual_pop);
        scrollContainer = findViewById(R.id.scroll_container);
        manualItemId = findViewById(R.id.manual_item_id);
        searchManualBtn = findViewById(R.id.search_manual_btn);
        manualEntryBtn = findViewById(R.id.manual_entry_btn);
        cancelItemBtn = findViewById(R.id.cancel_item);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        manualItemId.setAdapter(adapter);

        manualItemId.setOnItemClickListener((parent, view, position, id) -> {
            String selection = (String) parent.getItemAtPosition(position);
            manualItemId.setText(selection);
            searchManualBtn.performClick();
        });

        manualItemId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (debounceRunnable != null) {
                    debounceHandler.removeCallbacks(debounceRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    debounceRunnable = () -> searchItemsByName(query);
                    debounceHandler.postDelayed(debounceRunnable, 500);
                } else {
                    adapter.clear();
                }
            }
        });

        codeData.setText("Enter Username");
        toggleButton.setVisibility(View.GONE);
        saveItemBtn.setVisibility(View.GONE);
        manualEntryBtn.setVisibility(View.GONE);
        cancelItemBtn.setVisibility(View.GONE);
        dataPop.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            // Restore the saved state

            count = savedInstanceState.getInt(KEY_COUNT, 0);
        }


        manualEntryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (manualPop.getVisibility() == View.VISIBLE) {
                    startScanning();
                } else {
                    manualPop.setVisibility(View.VISIBLE);
                    dataPop.setVisibility(View.GONE);
                    codeScanner.stopPreview();
                    manualEntryBtn.setText("Scan");

                    // Make scroll container full screen
                    expandScrollContainer();
                }
            }
        });

        searchManualBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String manualId = manualItemId.getText().toString().trim();
                if (manualId.isEmpty()) {
                    manualItemId.setError("Enter Item ID");
                } else {
                    hideKeyboard();
                    itemID = manualId;
                    checkItem();
                    manualPop.setVisibility(View.GONE);
                    dataPop.setVisibility(View.VISIBLE);
                    manualEntryBtn.setText("Manual");

                    // Reset scroll container position after search success
                    resetScrollContainer();
                }
            }
        });

        cancelItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelItem();
            }
        });

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
        checkCameraPermission();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                // Start preview immediately if we are not in manual mode
                if (manualPop.getVisibility() == View.GONE) {
                    codeScanner.startPreview();
                }
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
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
                                    startScanning();
                                    userPop.setVisibility(View.GONE);
                                } else {
                                    customerUsername.setError("Customer does not exist");
                                }
                            } else {
                                customerUsername.setError("Username does not exist");
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
                        hideKeyboard();
                        checkItem();
                    }
                });
            }
        });
    }

    private void searchItemsByName(String nameQuery) {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("items");
        // Firebase doesn't support full-text search or case-insensitive contains easily
        // We can use startAt/endAt for simple prefix search
        // To handle middle-of-string matches better without a search engine like Algolia, 
        // we use a prefix search. For "oil" matching "Hair Oil", we'd need to fetch all and filter client-side.
        
        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();
                itemNameToIdMap.clear();
                String queryLower = nameQuery.toLowerCase();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String itemName = itemSnapshot.child("itemName").getValue(String.class);
                    String itemId = itemSnapshot.child("itemId").getValue(String.class);
                    if (itemName != null && itemId != null) {
                        if (itemName.toLowerCase().contains(queryLower)) {
                            adapter.add(itemName);
                            itemNameToIdMap.put(itemName, itemId);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                if (adapter.getCount() > 0) {
                    manualItemId.showDropDown();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void checkItem() {
        String input = manualItemId.getText().toString().trim();
        if (!input.isEmpty()) {
            if (itemNameToIdMap.containsKey(input)) {
                ItemCode = itemNameToIdMap.get(input);
            } else {
                ItemCode = input;
            }
        } else {
            ItemCode = itemID;
        }

        if (ItemCode == null || ItemCode.isEmpty()) {
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("items");
        Query checkItemDatabase = reference.orderByChild("itemId").equalTo(ItemCode);
        checkItemDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    codeData.setText("success !!");
                    manualEntryBtn.setVisibility(View.GONE);
                    cancelItemBtn.setVisibility(View.VISIBLE);
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
                    codeData.setText("item does not exist");
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
        manualEntryBtn.setVisibility(View.VISIBLE);
        cancelItemBtn.setVisibility(View.GONE);
        manualPop.setVisibility(View.GONE);
        dataPop.setVisibility(View.VISIBLE);
        toggleButton.setVisibility(View.VISIBLE);
        saveItemBtn.setVisibility(View.VISIBLE);
        manualEntryBtn.setText("Manual");
        hideKeyboard();
        resetScrollContainer();
    }

    private void resetScrollContainer() {
        if (scrollContainer != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scrollContainer.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.scanner_view);
            params.topMargin = (int) (-20 * getResources().getDisplayMetrics().density);
            scrollContainer.setLayoutParams(params);
        }
    }

    private void expandScrollContainer() {
        if (scrollContainer != null) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scrollContainer.getLayoutParams();
            params.removeRule(RelativeLayout.BELOW);
            params.topMargin = 0;
            scrollContainer.setLayoutParams(params);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void cancelItem() {
        startScanning();
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
        manualItemId.setText("");
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
        if (codeScanner != null) {
            codeScanner.releaseResources();
        }
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

        // Restart camera if permission is granted and we are not in manual mode
        if (manualPop.getVisibility() == View.GONE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                if (codeScanner != null) {
                    codeScanner.startPreview();
                }
            }
        }
    }
}

package com.example.billifyfinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.scales.Linear;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ShopHome extends AppCompatActivity {


    private double finalTotal;
     DatabaseReference billsRef;
    TextView amount;
    String userProfileName;
    ImageView settingbtn;
    Cartesian line ;
    List<DataEntry> data = new ArrayList<>();
    private int counter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_home);
        line = AnyChart.line();

        userProfileName = getIntent().getStringExtra("username");
        LinearLayout admin = findViewById(R.id.admin);
        TextView changeUsername = findViewById(R.id.username);
        ImageView profilePage = findViewById(R.id.profile_btn);
        ImageView scanBarcodepage = findViewById(R.id.scanBarcode);
        ImageView amtRef = findViewById(R.id.amt_ref);
        settingbtn = findViewById(R.id.setting_icon);
        LinearLayout recentBill = findViewById(R.id.recentBill);
        amount = findViewById(R.id.amount);

        if (userProfileName != null) {
            changeUsername.setText("@" + userProfileName);
        } else {
            Log.e("ShopHome", "User profile name is null");
            Toast.makeText(this, "Error: User profile name is null", Toast.LENGTH_SHORT).show();
        }

        profilePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profile = new Intent(ShopHome.this, Shopeprofile.class);
                profile.putExtra("username" , userProfileName);
                startActivity(profile);
            }
        });

        scanBarcodepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent barcode = new Intent(ShopHome.this, ScanBarcode.class);
                barcode.putExtra("username", userProfileName);
                startActivity(barcode);
            }
        });

        recentBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent history = new Intent(ShopHome.this, HistoryPage.class);
                history.putExtra("shopID", userProfileName);
                startActivity(history);
            }
        });

        settingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ShopHome.this,setting.class));
            }
        });

       refreshBtn();

       amtRef.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
                amount.setText("₹ " + 0.0);
                finalTotal = 0.0;
               refreshBtn();
           }
       });


        AnyChartView anyChartView = (AnyChartView) findViewById(R.id.any_chart_view);
        anyChartView.setChart(line);
        line.background().fill("#e6e6e6");
        Linear yScale = line.yScale();
        yScale.minimum(0);
        yScale.maximum(700);
    }


        private void refreshBtn() {
        data.clear();
        counter =1;
        billsRef = FirebaseDatabase.getInstance().getReference("Bills");
            billsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Get today's date
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
                    String todayDate = dateFormat.format(calendar.getTime());

                    // Iterate through the children nodes of the "Bills" node
                    for (DataSnapshot billIdSnapshot : dataSnapshot.getChildren()) {
                        // Check if the current bill is for today and matches the shopkeeper username
                        String shopkeeper = billIdSnapshot.child("shopkeeperUsername").getValue(String.class);
                        String dateFromDb = billIdSnapshot.child("date").getValue(String.class);
                        if (shopkeeper != null && shopkeeper.equals(userProfileName) && dateFromDb != null && dateFromDb.startsWith(todayDate)) {
                            String status = billIdSnapshot.child("status").getValue(String.class);
                            if (status != null && status.equals("Checked")) {
                                Double total = billIdSnapshot.child("grandTotal").getValue(Double.class);
                                if (total != null) {
                                    finalTotal += total;
                                    data.add(new ValueDataEntry("Bill " + counter, total));
                                    counter++;
                                }
                            }
                        }
                    }
                                    line.data(data);

                    DecimalFormat df = new DecimalFormat("#.##"); // Define your desired format
                    String formattedValue = df.format(finalTotal);

                    amount.setText("₹ " + formattedValue);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                    Log.e("Firebase", "Error getting data: " + databaseError.getMessage());
                }
            });
        }
}

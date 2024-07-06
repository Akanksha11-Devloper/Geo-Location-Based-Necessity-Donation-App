package com.example.necessity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.necessity.DonateActivities.DonateActivity;
import com.example.necessity.HistoryActivities.HistoryActivity;
import com.example.necessity.LoginSignup.landingpage;
import com.example.necessity.SendNotificationPack.MyService;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivityDonar extends AppCompatActivity {
    CardView donate, logout, ngomap, contact, mypin, history;
    FirebaseAuth fAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_donar);

        Intent intent = new Intent(MainActivityDonar.this, MyService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        donate = findViewById(R.id.cardDonate);
        logout = findViewById(R.id.cardLogout);
        mypin = findViewById(R.id.cardMyPin);
        history = findViewById(R.id.cardHistory);
        ngomap = findViewById(R.id.cardNgoMap);
        contact = findViewById(R.id.cardContact);
        fAuth= FirebaseAuth.getInstance();

        donate.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DonateActivity.class);
                startActivity(intent);
            }
        });
        ngomap.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Donar_food_map.class);
                startActivity(intent);
            }
        });
        mypin.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyPinNgo.class);
                startActivity(intent);
            }
        });
        history.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                intent.putExtra("isNGO", false);
                startActivity(intent);
            }
        });
        contact.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Contact.class);
                startActivity(intent);
            }
        });
        logout.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                stopService(new Intent(MainActivityDonar.this, MyService.class));
                Intent intent = new Intent(MainActivityDonar.this, landingpage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}
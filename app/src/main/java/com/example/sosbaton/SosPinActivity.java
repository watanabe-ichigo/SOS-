package com.example.sosbaton;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.Button;
import android.content.Intent;
import android.net.Uri;
import android.app.AlertDialog;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SosPinActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sospin);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button IncidentButton = findViewById(R.id.IncidentButton);
        IncidentButton.setOnClickListener(v -> finish());

        if (IncidentButton != null) {
            IncidentButton.setOnClickListener(v -> {
                Intent intent = new Intent(SosPinActivity.this, IncidentActivity.class);
                startActivity(intent);
            });
        }

        Button AccidentButton = findViewById(R.id.AccidentButton);
        AccidentButton.setOnClickListener(v -> finish());

        if (AccidentButton != null) {
            AccidentButton.setOnClickListener(v -> {
                Intent intent = new Intent(SosPinActivity.this, AccidentActivity.class);
                startActivity(intent);
            });
        }
    }
}

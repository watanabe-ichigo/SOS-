
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

public class SosActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private String UserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        UserName = getIntent().getStringExtra("UserName");
        if (UserName == null) UserName = "ゲスト";

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button HelpButton = findViewById(R.id.HelpButton);
        HelpButton.setOnClickListener(v -> showConfirmDialog());

        Button EmergencyButton = findViewById(R.id.EmergencyButton);
        EmergencyButton.setOnClickListener(v -> showConfirmDialogFor110());
    }

    // 119 → 確認ダイアログ
    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("確認")
                .setMessage("第三者に位置情報を共有しますか？")
                .setPositiveButton("OK", (dialog, which) -> {
                    startDial119();
                    sendLocationToPinsCollection(1); // type = 1 → SOS
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }

    // 110 → 確認ダイアログ
    private void showConfirmDialogFor110() {
        new AlertDialog.Builder(this)
                .setTitle("確認")
                .setMessage("第三者に位置情報を共有しますか？")
                .setPositiveButton("OK", (dialog, which) -> {
                    startDial110();
                    sendLocationToPinsCollection(2); // type = 2 → 110
                })
                .setNegativeButton("キャンセル", null)
                .show();
    }

    // 119 に電話
    private void startDial119() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:119"));
        startActivity(intent);
    }

    // 110 に電話
    private void startDial110() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:110"));
        startActivity(intent);
    }

    // 現在地取得 → pins コレクションに追加（typeで区別）
    private void sendLocationToPinsCollection(int type) {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1000
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {

                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        // まず最後の id を取得
                        db.collection("pins")
                                .orderBy("id", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    long newId = 1;
                                    if (!querySnapshot.isEmpty()) {
                                        long lastId = querySnapshot.getDocuments().get(0).getLong("id");
                                        newId = lastId + 1;
                                    }

                                    // pins コレクション用のデータ
                                    Map<String, Object> pinData = new HashMap<>();
                                    pinData.put("id", newId); // 連番
                                    pinData.put("lat_x", lat);
                                    pinData.put("lng_y", lng);
                                    pinData.put("name", UserName);
                                    pinData.put("type", 2);

                                    // ドキュメントを自動生成
                                    db.collection("pins")
                                            .add(pinData)
                                            .addOnSuccessListener(docRef -> {
                                                System.out.println("ピンを追加: " + docRef.getId());
                                            })
                                            .addOnFailureListener(e -> e.printStackTrace());
                                });
                    }
                });
    }
}





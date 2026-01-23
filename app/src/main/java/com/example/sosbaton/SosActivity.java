
package com.example.sosbaton;

import android.animation.ValueAnimator;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Button;
import android.content.Intent;
import android.net.Uri;
import android.app.AlertDialog;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class SosActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        userName = getIntent().getStringExtra("username");
        if (userName == null) userName = "ゲスト";

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        Button HelpButton = findViewById(R.id.HelpButton);
        HelpButton.setOnClickListener(v -> showConfirmDialog());

        Button EmergencyButton = findViewById(R.id.EmergencyButton);
        EmergencyButton.setOnClickListener(v -> showConfirmDialogFor110());

        Button DisasterButton = findViewById(R.id.DisasterButton);
        DisasterButton.setOnClickListener(v -> showConfirmDialogFor171());

        Button btnHowToUse = findViewById(R.id.btnHowToUse);


        btnHowToUse.setOnClickListener(v -> showDialog());




    }

    // 119 → 確認ダイアログ
    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("確認")
                .setMessage("第三者に位置情報を共有しますか？")
                .setPositiveButton("OK", (dialog, which) -> {
                    startDial119();
                    sendLocationToPinsCollection(3);
                })
                .setNegativeButton("キャンセル", (dialog, which) -> {
                    // キャンセルでも電話をかける
                    startDial119();
                })
                .show();
    }



    // 110 → 確認ダイアログ
    private void showConfirmDialogFor110() {
        new AlertDialog.Builder(this)
                .setTitle("確認")
                .setMessage("第三者に位置情報を共有しますか？")
                .setPositiveButton("OK", (dialog, which) -> {
                    startDial110();


                    sendLocationToPinsCollection(3); // type = 3 → sos
                })
                .setNegativeButton("キャンセル", (dialog, which) -> {
                    // キャンセルでも電話
                    startDial110();
                })
                .show();
    }


    private void showConfirmDialogFor171() {
        new AlertDialog.Builder(this)
                .setTitle("確認")
                .setMessage("第三者に位置情報を共有しますか？")
                .setPositiveButton("OK", (dialog, which) -> {
                    startDial110();


                    sendLocationToPinsCollection(3); // type = 3 → sos
                })
                .setNegativeButton("キャンセル", (dialog, which) -> {
                    // キャンセルでも電話
                    startDial171();
                })
                .show();
    }


    private void showDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_how_to_use, null);

// ダイアログを作成
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create(); // showの前に一度createする

// 自作XML内のボタンを取得して、クリックで閉じるようにする
        Button btnClose = view.findViewById(R.id.btn_close_dialog);
        btnClose.setOnClickListener(v -> {
            // ここに閉じるときの処理を書く
            dialog.dismiss();
        });

        dialog.show();

    }

    private void sendLocationToPinsCollection(
            int createdAt,
            int name,
            int pinType,
            int sosCategory,
            int urgency,
            int supportType,
            String uid
    ) {

        // ① 位置情報の権限チェック
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1000
            );
            return;
        }

        // ② 現在地取得
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) return;

                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    // ③ Firestoreに保存するデータ
                    Map<String, Object> pinData = new HashMap<>();
                    pinData.put("lat_x", lat);
                    pinData.put("lng_y", lng);
                    pinData.put("pinType", pinType);
                    pinData.put("urgency", urgency);
                    pinData.put("sosCategory", sosCategory);
                    pinData.put("supportType", supportType);
                    pinData.put("uid", uid);
                    pinData.put("name", userName);
                    pinData.put("createdAt", Timestamp.now());

                    // ④ Firestoreへ保存
                    db.collection("pins")
                            .add(pinData)
                            .addOnSuccessListener(docRef -> {
                                Log.d("PIN", "ピン追加成功: " + docRef.getId());
                            })
                            .addOnFailureListener(e -> {
                                Log.e("PIN", "保存失敗", e);
                            });
                });
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

    // 171 に電話
    private void startDial171() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:171"));
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
                                    pinData.put("name",userName);
                                    pinData.put("type", type);

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






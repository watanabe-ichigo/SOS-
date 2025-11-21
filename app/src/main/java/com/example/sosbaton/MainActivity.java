package com.example.sosbaton;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import java.util.Map;
import java.util.HashMap;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationRequest;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private MapView mapView;
    private static final String TAG = "Firestore";

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private Marker myMarker;
//    private LocationCallback locationCallback;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // EdgeToEdge
        EdgeToEdge.enable(this);

        // Firebase 初期化
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // レイアウトセット
        setContentView(R.layout.activity_main);
        //SOSでユーザネームを取得
        ImageButton btn_call = findViewById(R.id.btn_call);
        if (btn_call != null) {
            btn_call.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SosActivity.class);

                // headerView から TextView を取得して username を Intent に入れる
                View headerView = navigationView.getHeaderView(0);
                TextView tvUserName = headerView.findViewById(R.id.tvUserName);
                String username = tvUserName != null ? tvUserName.getText().toString() : "ゲスト";

                intent.putExtra("username", username);
                startActivity(intent);
            });
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // --- View取得 ---
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);
        mapView = findViewById(R.id.mapView);

        setSupportActionBar(toolbar);

        // --- ログイン中ユーザー情報を取得してヘッダーに表示 ---
        FirebaseUser currentUser = auth.getCurrentUser();
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tvUserName);

        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("username");
                            if (tvUserName != null) tvUserName.setText(name + " さん");

                            // 🔹 アイコン表示したい場合
                            // ImageView ivUserIcon = headerView.findViewById(R.id.ivUserIcon);
                            // String iconUrl = document.getString("iconUrl");
                            // Glide.with(this).load(iconUrl).into(ivUserIcon);

                        } else {
                            Log.d(TAG, "Firestore にドキュメントが存在しません");
                        }
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Firestore 取得失敗", e));
        } else {
            if (tvUserName != null) tvUserName.setText("ゲスト");
        }

        // --- Drawer 開閉 ---
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // --- NavigationView メニュー ---
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_settings) {
                // 設定
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // --- EdgeToEdge対応 ---
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- MapView 初期化 ---
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }

        // --- SOSボタン ---




        if (btn_call != null) {
            btn_call.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SosActivity.class);
                startActivity(intent);
            });
        }

        LinearLayout bottomMenu = findViewById(R.id.bottom_menu);

        ViewCompat.setOnApplyWindowInsetsListener(bottomMenu, (v, insets) -> {
            int bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(0, 0, 0, bottom);  // ← 下にだけナビバーの高さを足す
            return insets;
        });


        // --- Firebase 初期化 ---
        FirebaseApp.initializeApp(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();








    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    // --- 現在地赤ピン ---
    private void setCurrentLocationMarker() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        googleMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.addMarker(new MarkerOptions()
                                .position(current)
                                .title("現在地")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
                        Log.d(TAG, "現在地取得成功: " + location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        Log.d(TAG, "現在地が取得できませんでした");
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        setCurrentLocationMarker();

        // --- Firestoreからpinsを取得してマップにピンを立てる ---
        db.collection("pins")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Double lat = doc.getDouble("lat_x");
                        Double lng = doc.getDouble("lng_y");
                        String name = doc.getString("name");

                        if (lat != null && lng != null) {
                            LatLng pinPosition = new LatLng(lat, lng);
                            Marker marker = googleMap.addMarker(new MarkerOptions()
                                    .position(pinPosition)
                                    .title(name != null ? name : "未設定ピン")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            );

                            if (marker != null) {
                                // Firestore ドキュメントIDをタグにセット
                                marker.setTag(doc.getId());
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w("FirestorePin", "取得失敗", e));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }

        // --- 赤ピン / 緑ピン追加ボタン ---
        ImageButton btnPin = findViewById(R.id.btn_pin);   // 緑ピン
        ImageButton btnPost = findViewById(R.id.btn_post); // 赤ピン

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tvUserName);
        String userName = tvUserName != null ? tvUserName.getText().toString() : "ゲスト";

        // 🔴 赤ピン
        btnPost.setOnClickListener(v -> {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

                            // Firestore 保存
                            Map<String, Object> pinData = new HashMap<>();
                            pinData.put("lat_x", pos.latitude);
                            pinData.put("lng_y", pos.longitude);
                            pinData.put("name", userName);
                            pinData.put("type", 1); // 赤ピン

                            db.collection("pins")
                                    .add(pinData)
                                    .addOnSuccessListener(docRef -> {
                                        Log.d(TAG, "赤ピン保存成功: " + docRef.getId());

                                        // マーカー追加 & タグ設定
                                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                                .position(pos)
                                                .title("赤ピン")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                        );
                                        if (marker != null) marker.setTag(docRef.getId());
                                    })
                                    .addOnFailureListener(e -> Log.w(TAG, "赤ピン保存失敗", e));
                        }
                    });
        });

        // 🟢 緑ピン
        btnPin.setOnClickListener(v -> {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

                            // Firestore 保存
                            Map<String, Object> pinData = new HashMap<>();
                            pinData.put("lat_x", pos.latitude);
                            pinData.put("lng_y", pos.longitude);
                            pinData.put("name", userName);
                            pinData.put("type", 2); // 緑ピン

                            db.collection("pins")
                                    .add(pinData)
                                    .addOnSuccessListener(docRef -> {
                                        Log.d(TAG, "緑ピン保存成功: " + docRef.getId());

                                        // マーカー追加 & タグ設定
                                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                                .position(pos)
                                                .title("緑ピン")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                        );
                                        if (marker != null) marker.setTag(docRef.getId());
                                    })
                                    .addOnFailureListener(e -> Log.w(TAG, "緑ピン保存失敗", e));
                        }
                    });
        });

        // --- マーカー削除用リスナー ---
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag != null && tag instanceof String) {
                String docId = (String) tag;

                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("ピン削除")
                        .setMessage("本当にこのピンを削除しますか？")
                        .setPositiveButton("削除", (dialog, which) -> {
                            db.collection("pins").document(docId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "ピン削除成功: " + docId))
                                    .addOnFailureListener(e -> Log.w(TAG, "ピン削除失敗", e));
                            marker.remove();
                        })
                        .setNegativeButton("キャンセル", (dialog, which) -> dialog.dismiss())
                        .show();
            }
            return true; // InfoWindowは表示しない
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setCurrentLocationMarker();
                startLocationUpdates();
                Log.d(TAG, "位置情報権限が許可されました");
            } else {
                Log.d(TAG, "位置情報権限が拒否されました");
            }
        }
    }

    private com.google.android.gms.location.LocationCallback locationCallback =
            new com.google.android.gms.location.LocationCallback() {
                @Override
                public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                    if (locationResult == null) return;

                    android.location.Location location = locationResult.getLastLocation();
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());

                    if (myMarker == null) {
                        myMarker = googleMap.addMarker(
                                new MarkerOptions()
                                        .position(current)
                                        .title("現在地（追尾）")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        );
                    } else {
                        myMarker.setPosition(current);
                    }

//                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 17));
                }
            };

    // --- 位置情報追尾開始 ---
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000); // 3秒ごと
        locationRequest.setFastestInterval(1000); // 最短1秒ごとに更新
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                getMainLooper()
        );
    }

    // --- Firestore にピン保存（赤=1, 緑=2） ---
    private void savePinToFirestore(double lat, double lng, String userName, int type) {

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

                    Map<String, Object> pinData = new HashMap<>();
                    pinData.put("id", newId);
                    pinData.put("lat_x", lat);
                    pinData.put("lng_y", lng);
                    pinData.put("name", userName);
                    pinData.put("type", type);

                    db.collection("pins")
                            .add(pinData)
                            .addOnSuccessListener(docRef -> Log.d("Firestore", "ピン保存成功: " + docRef.getId()))
                            .addOnFailureListener(e -> Log.w("Firestore", "ピン保存失敗", e));
                });
    }
}
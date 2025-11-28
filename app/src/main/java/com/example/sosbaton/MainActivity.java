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
import android.app.AlertDialog;
import java.util.List;
import com.google.android.gms.maps.model.LatLng;
import com.example.sosbaton.BuildConfig;




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


        // レイアウトセット
        setContentView(R.layout.activity_main);


        // --- View取得 ---
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);
        mapView = findViewById(R.id.mapView);


        // EdgeToEdge
        EdgeToEdge.enable(this);

        // Firebase 初期化
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


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
                startActivity(new Intent(MainActivity.this, StartActivity.class));
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


        //経路選択
        Button btnEvacuate = findViewById(R.id.btevacuation);
        //避難所はとりあえず開成山公園に設定
        LatLng evacuationPoint = new LatLng(37.39830881, 140.35796203);


        btnEvacuate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("ルート選択")
                        .setMessage("避難方法を選択してください")
                        .setPositiveButton("危険回避ルート", (dialog, which) -> {
                            //drawRouteAvoiding(evacuationPoint);
                        })
                        .setNegativeButton("安全経由ルート", (dialog, which) -> {
                            //drawRouteDirect(evacuationPoint);
                        })
                        .setNeutralButton("最短ルート", (dialog, which) -> {
                            drawRouteShortest(evacuationPoint);
                        })
                        .show();
            }
        });


        Log.d("TEST", "MAPS=" + BuildConfig.MAPS_API_KEY);

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

        // userName取得
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tvUserName);
        String userName = tvUserName != null ? tvUserName.getText().toString() : "ゲスト";

        //------------------------------------------------------------
        // ① マップタップでメニュー
        //------------------------------------------------------------
        googleMap.setOnMapClickListener(latLng -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("ここで何をする？")
                    .setItems(new CharSequence[]{"赤ピン", "緑ピン", "ここへ行く", "キャンセル"},
                            (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        addPin(latLng, userName, 1);
                                        break;
                                    case 1:
                                        addPin(latLng, userName, 2);
                                        break;
                                    case 2:
                                        drawRouteShortest(latLng);
                                        break;
                                }
                            })
                    .show();
        });

        //------------------------------------------------------------
        // ② 現在地ピン
        //------------------------------------------------------------
        setCurrentLocationMarker();

        //------------------------------------------------------------
        // ③ Firestore ピン読込
        //------------------------------------------------------------
        loadPinsFromFirestore();

        //------------------------------------------------------------
        // ④ マーカークリック（ここへ行く・削除）
        //------------------------------------------------------------
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag != null) {
                String docId = (String) tag;

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("ピン操作")
                        .setItems(new CharSequence[]{"ここへ行く", "削除", "キャンセル"},
                                (dialog, which) -> {
                                    switch (which) {
                                        case 0:
                                            drawRouteShortest(marker.getPosition());
                                            break;
                                        case 1:
                                            deletePin(marker, docId);
                                            break;
                                    }
                                })
                        .show();
            }
            return true;
        });

        //------------------------------------------------------------
        // ⑤ 現在地追尾
        //------------------------------------------------------------
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }


    // ---------------------------
    // 現在地ピン
    // ---------------------------
//    private void setCurrentLocationMarker() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            return;
//        }
//
//        googleMap.setMyLocationEnabled(true);
//
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(location -> {
//                    if (location != null) {
//                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                        googleMap.addMarker(
//                                new MarkerOptions().position(latLng).title("現在地")
//                        );
//                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
//                    }
//                });
//    }


    // ---------------------------
    // Firestore ピン保存（赤1 / 緑2）
    // ---------------------------
    private void addPin(LatLng pos, String userName, int type) {

        Map<String, Object> pinData = new HashMap<>();
        pinData.put("lat_x", pos.latitude);
        pinData.put("lng_y", pos.longitude);
        pinData.put("name", userName);
        pinData.put("type", type);

        db.collection("pins")
                .add(pinData)
                .addOnSuccessListener(docRef -> {

                    float color = (type == 1)
                            ? BitmapDescriptorFactory.HUE_RED
                            : BitmapDescriptorFactory.HUE_GREEN;

                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(type == 1 ? "赤ピン" : "緑ピン")
                            .icon(BitmapDescriptorFactory.defaultMarker(color))
                    );

                    if (marker != null) marker.setTag(docRef.getId());
                });
    }


    // ---------------------------
    // Firestore ピン読込
    // ---------------------------
    private void loadPinsFromFirestore() {
        db.collection("pins")
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {

                        Double lat = doc.getDouble("lat_x");
                        Double lng = doc.getDouble("lng_y");
                        String name = doc.getString("name");
                        Long type = doc.getLong("type");

                        if (lat == null || lng == null) continue;

                        LatLng pos = new LatLng(lat, lng);

                        float color;
                        if (type != null && type == 1) color = BitmapDescriptorFactory.HUE_RED;
                        else if (type != null && type == 2) color = BitmapDescriptorFactory.HUE_GREEN;
                        else color = BitmapDescriptorFactory.HUE_BLUE;

                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(name != null ? name : "未設定ピン")
                                .icon(BitmapDescriptorFactory.defaultMarker(color))
                        );

                        if (marker != null) marker.setTag(doc.getId());
                    }
                });
    }


    // ---------------------------
    // ピン削除
    // ---------------------------
//    private void deletePin(Marker marker, String docId) {
//        new AlertDialog.Builder(MainActivity.this)
//                .setTitle("削除確認")
//                .setMessage("このピンを削除しますか？")
//                .setPositiveButton("削除", (d, w) -> {
//                    db.collection("pins").document(docId)
//                            .delete()
//                            .addOnSuccessListener(x -> marker.remove());
//                })
//                .setNegativeButton("キャンセル", null)
//                .show();
//    }


    // ---------------------------
    // 現在地追尾
    // ---------------------------
//    private LocationCallback locationCallback = new LocationCallback() {
//        @Override
//        public void onLocationResult(LocationResult result) {
//            if (result == null) return;
//
//            LatLng current = new LatLng(
//                    result.getLastLocation().getLatitude(),
//                    result.getLastLocation().getLongitude()
//            );
//
//            if (myMarker == null) {
//                myMarker = googleMap.addMarker(new MarkerOptions()
//                        .position(current)
//                        .title("現在地（追尾）")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//                );
//            } else {
//                myMarker.setPosition(current);
//            }
//        }
//    };









    @Override
    public void onRequestPermissionsResult ( int requestCode, String[] permissions,
                                             int[] grantResults){
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
    private void startLocationUpdates () {
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
    private void savePinToFirestore ( double lat, double lng, String userName,int type){

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








    //最短ルートが押された時に呼び出されるルート検索関数
    private void drawRouteShortest(LatLng destination) {

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // 許可済 → 位置情報取得
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                            fetchRoute(origin, destination);
                        }
                    });

        } else {
            return;
        }

    }

    //ルート計算関数(計算自体はGoogleAPIなのでHTTP通信するためのロジック)
    private void fetchRoute(LatLng origin, LatLng destination) {

//APIへのURL作成
        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + origin.latitude + "," + origin.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude
                + "&mode=walking"
                + "&alternatives=true"
                + "&key=" + BuildConfig.MAPS_API_KEY; // ← local.properties のキーを参照


//メインスレッド（今回はMAP画面)でのHTTP通信はルール上禁止→別スレッド（バックグラウンド）での処理にする）
        new Thread(() -> {
            try {
                java.net.URL reqUrl = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) reqUrl.openConnection();
                conn.connect();
                java.io.InputStreamReader isr = new java.io.InputStreamReader(conn.getInputStream());
                java.io.BufferedReader reader = new java.io.BufferedReader(isr);

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);

                parseRouteJson(sb.toString());
            } catch (Exception e) {
                Log.e("RouteFetch", "ルート取得失敗: ", e);
            }
        }).start();//←別スレッドの起動


    }

    // ④取得した道案内データを解析＆Polyline 描画関数
    private void parseRouteJson(String json) {
        try {

            //返ってきたJSONデータ（string形でこのままでは使えない)を扱えるようオブジェクト化する
            org.json.JSONObject jsonObject = new org.json.JSONObject(json);

            //JSONデータからroutesを取り出す
            org.json.JSONArray routes = jsonObject.getJSONArray("routes");
            if (routes.length() == 0) return;


            org.json.JSONObject route = routes.getJSONObject(0);
            org.json.JSONObject polyline = route.getJSONObject("overview_polyline");
            String encoded = polyline.getString("points");


            List<LatLng> points = decodePolyline(encoded);


            //UI操作はメインスレッドの特権（現在は別スレッドなのでメインに戻す)
            runOnUiThread(() -> {

                //実際のUI操作（経路の表示)
                googleMap.addPolyline(new com.google.android.gms.maps.model.PolylineOptions()
                        .addAll(points)
                        .width(12)//←線の太さ
                        .color(android.graphics.Color.BLUE) // 線の色
                        .geodesic(true)//曲面に沿った自然な線にする
                );
            });

        } catch (Exception e) {
            Log.e("RouteParse", "解析失敗: ", e);
        }
    }


    // Google Polyline をデコード（圧縮データの解凍)する関数
    private List<LatLng> decodePolyline(String encoded) {

        List<LatLng> poly = new java.util.ArrayList<>();


        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;

                //5ビットずつ座標データを復元していく
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            //dlat: 変化量（暗号） → 本来の緯度差に戻す.前の値 lat に加算して 絶対値に戻す(緯度計算)
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {

                //同じように経度計算
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }

        return poly;
    }


    //ピンの削除関数
    private void deletePin (Marker marker, String docId){
        new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                .setTitle("ピン削除")
                .setMessage("本当にこのピンを削除しますか？")
                .setPositiveButton("削除", (dialog, which) -> {
                    db.collection("pins").document(docId)
                            .delete()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "ピン削除成功: " + docId))
                            .addOnFailureListener(e -> Log.w(TAG, "ピン削除失敗", e));
                    marker.remove();  // マップから削除
                })
                .setNegativeButton("キャンセル", (dialog, which) -> dialog.dismiss())
                .show();
    }
}




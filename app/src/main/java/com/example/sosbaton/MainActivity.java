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
import java.util.ArrayList;
import com.google.android.gms.maps.model.LatLng;
import com.example.sosbaton.BuildConfig;
import android.location.Location;
import android.widget.Toast;

import com.google.firebase.auth.UserProfileChangeRequest;




public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private String userName = "ゲスト";
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private MapView mapView;
    private static final String TAG = "Firestore";

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private List<Marker> allMarkers = new ArrayList<>();

    private Marker myMarker;
//    private LocationCallback locationCallback;

    private FirebaseFirestore db;

    private FirebaseAuth auth;

    private FirebaseUser currentUser;





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




// 起動時にログインユーザーをチェックするのだ！

        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            // ログイン状態が維持されているのだ！

            // 現在のユーザー名（displayName）をチェックするのだ！
            String displayName = currentUser.getDisplayName();
            userName = displayName;

            if (displayName != null && !displayName.isEmpty()) {
                // ① displayNameが既に設定されている場合なのだ！

                String welcomeMessage = "ようこそ、" + displayName + "なのだ！";
                Toast.makeText(this, welcomeMessage, Toast.LENGTH_LONG).show();
                // マップ画面など、アプリのメインコンテンツを表示するのだ。

            } else {
                // ② displayNameが未設定の場合 (Firestoreからusernameを取得するのだ！)
                String currentUid = currentUser.getUid();

                // 独自にusernameを保存しているコレクション（例: "users"）にアクセスするのだ！
                db.collection("users")
                        .document(currentUid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String registeredUsername = documentSnapshot.getString("username"); // データベースからusernameを取得するのだ

                            if (registeredUsername != null) {
                                // usernameがデータベースにあった場合なのだ！
                                userName = registeredUsername;

                                // FirebaseのdisplayNameも更新して、次回以降はすぐに取得できるようにするのだ！
                                updateFirebaseDisplayName(currentUser, registeredUsername);

                            } else {
                                // データベースにもusernameがない場合なのだ...
                                String welcomeMessage = "ようこそ、名無しさんなのだ！";
                                Toast.makeText(this, welcomeMessage, Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Firestoreからの取得に失敗した場合なのだ
                            Log.e(TAG, "ユーザー名の取得に失敗したのだ: " + e.getMessage());
                            String welcomeMessage = "ようこそ、名無しさんなのだ！";
                            Toast.makeText(this, welcomeMessage, Toast.LENGTH_LONG).show();
                        });
            }

        } else {
            // 誰もログインしていないのだ！
            Log.d(TAG, "ログインが必要なのだ。");
            // ログイン画面へ誘導するのだ。
        }



        //SOSでユーザネームを取得
        ImageButton btn_call = findViewById(R.id.btn_call);
        if (btn_call != null) {
            btn_call.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SosActivity.class);

                // headerView から TextView を取得して username を Intent に入れる
                View headerView = navigationView.getHeaderView(0);
                TextView tvUserName = headerView.findViewById(R.id.tvUserName);
                String username = tvUserName != null ? tvUserName.getText().toString() : "username";

                intent.putExtra("username", username);
                startActivity(intent);
            });
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        setSupportActionBar(toolbar);

        // --- ログイン中ユーザー情報を取得してヘッダーに表示 ---
        currentUser = auth.getCurrentUser();
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
        // FirebaseApp.initializeApp(this);
        // FirebaseFirestore db = FirebaseFirestore.getInstance();

        //経路選択
// --- btnEvacuate のクリック処理 ---
        Button btnEvacuate = findViewById(R.id.btevacuation);

        btnEvacuate.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("ルート選択")
                    .setMessage("避難方法を選択してください")
                    .setPositiveButton("危険回避ルート", (dialog, which) -> {

                        // 避難所リストを作成
                        List<LatLng> evacuationPoints = new ArrayList<>();
                        evacuationPoints.add(new LatLng(37.39830881, 140.35796203)); // 開成山公園
                        evacuationPoints.add(new LatLng(37.376782, 140.392777));     // 東部体育館
                        evacuationPoints.add(new LatLng(37.36942367, 140.37393403)); // ビッグパレット
                        evacuationPoints.add(new LatLng(37.419631, 140.390504));     // 富久山公民館

                        // 各避難所へのルート描画
                        for (LatLng dest : evacuationPoints) {
                            drawRouteAvoiding(dest);
                        }
                    })
                    .setNegativeButton("安全経由ルート", (dialog, which) -> {
                        // 安全ルートの処理をここに追加
                    })
                    .setNeutralButton("最短ルート", (dialog, which) -> {
                        // 例: 最短ルート
                        drawRouteShortest(new LatLng(37.39830881, 140.35796203));
                        drawRouteShortest(new LatLng(37.376782, 140.392777));     // 東部体育館
                        drawRouteShortest(new LatLng(37.36942367, 140.37393403)); // ビッグパレット
                        drawRouteShortest(new LatLng(37.419631, 140.390504));     // 富久山公民館
                    })
                    .show();
        });

        //経路選択
//        Button btnEvacuate = findViewById(R.id.btevacuation);
//        //避難所はとりあえず開成山公園に設定
//        LatLng evacuationPoint = new LatLng(37.39830881, 140.35796203);
//        LatLng evacuationPoint1 = new LatLng(37.401941, 140.403995);
//        LatLng evacuationPoint2 = new LatLng(37.391966, 140.359566);
//        LatLng evacuationPoint3 = new LatLng(37.420320, 140.374980);


//        btnEvacuate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new AlertDialog.Builder(MainActivity.this)
//                        .setTitle("ルート選択")
//                        .setMessage("避難方法を選択してください")
//                        .setPositiveButton("危険回避ルート", (dialog, which) -> {
//                            drawRouteAvoiding(evacuationPoint);
//                            drawRouteAvoiding(evacuationPoint1);
//                            drawRouteAvoiding(evacuationPoint2);
//                            drawRouteAvoiding(evacuationPoint3);
//                        })
//                        .setNegativeButton("安全経由ルート", (dialog, which) -> {
//                            //drawRouteDirect(evacuationPoint);
//                        })
//                        .setNeutralButton("最短ルート", (dialog, which) -> {
//                            drawRouteShortest(evacuationPoint);
//                           drawRouteAvoiding(evacuationPoint1);
//                            drawRouteAvoiding(evacuationPoint2);
//                            drawRouteAvoiding(evacuationPoint3);
//                        })
//                        .show();
//            }
//        });


//        Log.d("TEST", "MAPS=" + BuildConfig.MAPS_API_KEY);
//
    }

    // ----------------------------------------------------------------------
// 【補足：displayNameを更新する関数を別途作成するのだ！】

    private void updateFirebaseDisplayName(FirebaseUser user, String newDisplayName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Profile", "displayNameをusernameに更新完了なのだ！");

                        userName = newDisplayName;

                        // 更新完了後、ユーザー名でToast表示するのだ！
                        Toast.makeText(this, "ようこそ、" + newDisplayName + "なのだ！", Toast.LENGTH_LONG).show();
                    } else {
                        Log.w("Profile", "更新失敗なのだ。", task.getException());

                        userName = newDisplayName;

                        // 失敗した場合も、取得したusernameでとりあえずToast表示するのも手なのだ。
                        Toast.makeText(this, "ようこそ、" + newDisplayName + "なのだ！", Toast.LENGTH_LONG).show();
                    }
                });
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
                        LocationRequest request = LocationRequest.create();
                        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        request.setInterval(1000);
                        fusedLocationClient.requestLocationUpdates(request, locationCallback, getMainLooper());
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

        googleMap = map; // ← これを最初に置くのが絶対

        // --- タップでメニュー表示 ---
        googleMap.setOnMapClickListener(latLng -> {
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
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

                                    default:
                                        dialog.dismiss();
                                }
                            })
                    .show();
        });

        // --- 現在地 ---
        setCurrentLocationMarker();

        // --- 避難所ピンを直接追加 ---
        List<LatLng> evacuationPoints = new ArrayList<>();
        List<String> evacuationNames = new ArrayList<>();

        // ① 開成山公園
        evacuationPoints.add(new LatLng(37.39830881, 140.35796203));
        evacuationNames.add("開成山公園");

        // ② 東部体育館
        evacuationPoints.add(new LatLng(37.376782, 140.392777));
        evacuationNames.add("東部体育館");

        // ③ ビッグパレットふくしま
        evacuationPoints.add(new LatLng(37.36942367, 140.37393403));
        evacuationNames.add("ビッグパレットふくしま");

        // ④ 富久山公民館
        evacuationPoints.add(new LatLng(37.419631, 140.390504));
        evacuationNames.add("富久山公民館");

        // ループでマーカー作成
        for (int i = 0; i < evacuationPoints.size(); i++) {
            LatLng point = evacuationPoints.get(i);
            String name = evacuationNames.get(i);

            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title(name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );

            // 必要なら tag をセット
            if (marker != null) {
                marker.setTag("evacuation");
            }
        }

        // --- Firestore 読み込み ---
        loadPinsFromFirestore();

        // --- マーカークリックメニュー ---
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            String docId = null; // ドキュメントIDを格納する変数なのだ

            if (tag instanceof Map) {
                // Tag が Map の場合は、そこから docId を取り出すのだ
                Map<String, Object> tagData = (Map<String, Object>) tag;
                docId = (String) tagData.get("docId");

            } else if (tag instanceof String) {
                // Tag が String の場合は、それが docId だと見なすのだ（古いコードの名残かもしれないが）
                docId = (String) tag;

            }

            if (docId != null) { // docId が取得できたら処理を続けるのだ！

                String finalDocId = docId; // ラムダ式内で使うために final 化するのだ
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("ピン操作")
                        .setItems(new CharSequence[]{"ここへ行く", "削除", "キャンセル"},
                                (dialog, which) -> {
                                    switch (which) {
                                        case 0:
                                            drawRouteShortest(marker.getPosition());
                                            break;

                                        case 1:
                                            // 削除関数も docId を受け取るように修正が必要かもしれないのだ
                                            deletePin(marker, finalDocId);
                                            break;

                                        default:
                                            dialog.dismiss();
                                    }
                                })
                        .show();
            }
            // ここで return true; を忘れるな！
            return true;
        });

        // --- 権限あるなら位置更新 ---
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }
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
                    allMarkers.add(marker);


                    if (marker != null) {
                        // 修正点：TagにDocument IDと type を持つ HashMap を設定するのだ！
                        Map<String, Object> tagData = new HashMap<>();
                        tagData.put("docId", docRef.getId());
                        tagData.put("type", (long)type); // long型にキャストして合わせるのだ

                        marker.setTag(tagData);
                    }
                });
    }
    private void loadPinsFromFirestore() {
        db.collection("pins")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Double lat = doc.getDouble("lat_x");
                        Double lng = doc.getDouble("lng_y");
                        String name = doc.getString("name");
                        Long type = doc.getLong("type");

                        if (lat != null && lng != null) {
                            LatLng pinPosition = new LatLng(lat, lng);

                            float color;
                            if (type != null && type == 1) {
                                color = BitmapDescriptorFactory.HUE_RED;
                            } else if (type != null && type == 2) {
                                color = BitmapDescriptorFactory.HUE_GREEN;
                            } else {
                                color = BitmapDescriptorFactory.HUE_BLUE;
                            }

                            Marker marker = googleMap.addMarker(new MarkerOptions()
                                    .position(pinPosition)
                                    .title(name != null ? name : "未設定ピン")
                                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                            );

                            if (marker != null) {
                                Map<String, Object> tagData = new HashMap<>();
                                tagData.put("docId", doc.getId());
                                tagData.put("type", type); // ← これが重要！

                                marker.setTag(tagData);
                                allMarkers.add(marker);
                            }
                        }
                    }
                });
    }












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

    //危険回避ルートが押された時に呼び出されるルート検索関数達
    // --- helper: メートル単位で緯度経度をオフセットする ---
    private LatLng offsetLatLng(LatLng origin, double eastMeters, double northMeters) {
        // 地球半径 (m)
        double R = 6378137;
        double dLat = northMeters / R;
        double dLon = eastMeters / (R * Math.cos(Math.toRadians(origin.latitude)));
        double newLat = origin.latitude + Math.toDegrees(dLat);
        double newLon = origin.longitude + Math.toDegrees(dLon);
        return new LatLng(newLat, newLon);
    }

    // --- 追加 helper: danger の周囲に等間隔に候補点を作る ---
    private List<LatLng> generateCircularCandidates(LatLng center, double radiusMeters, int count) {
        List<LatLng> out = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double dx = Math.cos(angle) * radiusMeters; // 東方向成分（m）
            double dy = Math.sin(angle) * radiusMeters; // 北方向成分（m）
            out.add(offsetLatLng(center, dx, dy));
        }
        return out;
    }

    // --- 新しい fetch: 候補点を作って順に試す ---


    // --- まず直通ルートを試して、安全なら描画。ダメなら候補を順に試す ---
    private void tryRouteDirectThenCandidates(LatLng origin, LatLng destination,
                                              List<LatLng> waypointCandidates,
                                              int maxTrials,
                                              List<LatLng> dangerPins) {

        // 直通ルートを取得
        new Thread(() -> {
            try {
                String urlDirect = "https://maps.googleapis.com/maps/api/directions/json?"
                        + "origin=" + origin.latitude + "," + origin.longitude
                        + "&destination=" + destination.latitude + "," + destination.longitude
                        + "&mode=walking"
                        + "&alternatives=false"
                        + "&key=" + BuildConfig.MAPS_API_KEY;

                org.json.JSONObject jsonObj = requestJson(urlDirect);
                if (jsonObj != null) {
                    org.json.JSONArray routes = jsonObj.getJSONArray("routes");
                    if (routes.length() > 0) {
                        String encoded = routes.getJSONObject(0)
                                .getJSONObject("overview_polyline").getString("points");
                        List<LatLng> points = decodePolyline(encoded);
                        if (!passesThroughDanger(points, dangerPins, 50)) {
                            // 安全なら直ちに描画して終了
                            runOnUiThread(() -> {
                                googleMap.addPolyline(new com.google.android.gms.maps.model.PolylineOptions()
                                        .addAll(points).width(12).color(android.graphics.Color.MAGENTA).geodesic(true));
                            });
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("RouteAvoid", "直通ルート確認で例外: ", e);
            }

            // 直通がダメなら候補を順に試す
            // 並列で投げるとAPI制限に引っかかるかもしれないから順次同期的に試す
            int tried = 0;
            for (LatLng wp : waypointCandidates) {
                if (tried >= maxTrials) break;
                tried++;

                try {
                    // via: を使うことで必ずその地点を経由させる（経路を強制的に迂回させられる）
                    String waypointParam = "via:" + wp.latitude + "," + wp.longitude;
                    String url = "https://maps.googleapis.com/maps/api/directions/json?"
                            + "origin=" + origin.latitude + "," + origin.longitude
                            + "&destination=" + destination.latitude + "," + destination.longitude
                            + "&waypoints=" + java.net.URLEncoder.encode(waypointParam, "UTF-8")
                            + "&mode=walking"
                            + "&alternatives=false"
                            + "&key=" + BuildConfig.MAPS_API_KEY;

                    org.json.JSONObject jsonObj = requestJson(url);
                    if (jsonObj == null) continue;

                    org.json.JSONArray routes = jsonObj.getJSONArray("routes");
                    if (routes.length() == 0) continue;

                    String encoded = routes.getJSONObject(0)
                            .getJSONObject("overview_polyline").getString("points");
                    List<LatLng> points = decodePolyline(encoded);

                    // 返ってきたルートが危険ピンと被らなければ採用して終了
                    if (!passesThroughDanger(points, dangerPins, 50)) {
                        runOnUiThread(() -> {
                            googleMap.addPolyline(new com.google.android.gms.maps.model.PolylineOptions()
                                    .addAll(points).width(12).color(android.graphics.Color.MAGENTA).geodesic(true));
                        });
                        return;
                    }

                } catch (Exception e) {
                    Log.e("RouteAvoid", "候補試行で例外: ", e);
                }
            }

            // 全部ダメだったら UI に失敗表示
            runOnUiThread(() -> {
                Log.w("RouteAvoid", "安全に通れるルートが見つかりませんでした（候補全滅）");
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("危険回避ルートが見つかりません")
                        .setMessage("候補を試したが、危険ピンを回避できるルートが見つかりませんでした。")
                        .setPositiveButton("OK", null)
                        .show();
            });

        }).start();
    }

    // --- 単純な HTTP GET をして JSON を返すユーティリティ ---
    private org.json.JSONObject requestJson(String urlStr) {
        try {
            java.net.URL reqUrl = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) reqUrl.openConnection();
            conn.connect();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return new org.json.JSONObject(sb.toString());
        } catch (Exception e) {
            Log.e("RouteAvoid", "requestJson失敗: ", e);
            return null;
        }
    }

    private void drawRouteAvoiding(LatLng destination) {
        // 権限チェック
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // 権限がない場合はリクエストを出す
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }
        // dangerPinsを同期的に作る
        List<LatLng> dangerPins = new ArrayList<>();
        for (Marker m : allMarkers) {
            Object tag = m.getTag();
            if (tag instanceof Map) {
                Map<String, Object> tagData = (Map<String, Object>) tag;
                Long type = (Long) tagData.get("type");
                if (type != null && type == 1) { // 赤ピン
                    dangerPins.add(m.getPosition());
                }
            }
        }

        // 現在地取得
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());

                        List<LatLng> allCandidates = new ArrayList<>();
                        double candidateRadius = 300;
                        int candidateCount = 12;

                        for (LatLng dangerCenter : dangerPins) {
                            allCandidates.addAll(generateCircularCandidates(dangerCenter, candidateRadius, candidateCount));
                        }

                        tryRouteDirectThenCandidates(origin, destination, allCandidates, 30, dangerPins);
                    }
                });
    }
//    private void drawRouteAvoiding(LatLng destination) {
//
//        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) return;
//
//        // 【⭐ 危険ピン（赤ピン）を先に取得しておくのだ！ ⭐】
//        List<LatLng> dangerPins = new java.util.ArrayList<>();
//        // UIスレッドで実行する必要がある処理だから runOnUiThread で囲むのだ
//        runOnUiThread(() -> {
//            for (Marker m : allMarkers) {
//                Object tag = m.getTag();
//                if (tag instanceof Map) {
//                    Map<String, Object> tagData = (Map<String, Object>) tag;
//                    Long type = (Long) tagData.get("type");
//                    if (type != null && type == 1) { // type が 1（赤ピン）であれば危険ピン
//                        dangerPins.add(m.getPosition());
//                    }
//                }
//            }
//        });
//
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(location -> {
//                    if (location != null) {
//                        LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
//
//                        // 【⭐ 修正点：危険ピンの周囲に候補点を生成し、それを試すロジックを呼び出すのだ！ ⭐】
//                        List<LatLng> allCandidates = new ArrayList<>();
//                        double candidateRadius = 300; // 危険ピンから300m離れた円周上に候補を作る
//                        int candidateCount = 12; // 12方向に候補を作る
//
//                        // 全ての危険ピンの周囲に候補点を生成してリストに集めるのだ
//                        for (LatLng dangerCenter : dangerPins) {
//                            allCandidates.addAll(generateCircularCandidates(dangerCenter, candidateRadius, candidateCount));
//                        }
//                        // 直通がダメなら、この候補リストを順番に経由地として試すのだ！
//                        tryRouteDirectThenCandidates(origin, destination, allCandidates, 30, dangerPins); // 最大30個の候補を試す
//                    }
//                });
//    }


    private boolean passesThroughDanger(List<LatLng> routePoints,
                                        List<LatLng> dangerPins,
                                        double radiusMeters) {

        float[] results = new float[1];

        for (LatLng p : routePoints) {
            for (LatLng d : dangerPins) {
                Location.distanceBetween(
                        p.latitude, p.longitude,
                        d.latitude, d.longitude,
                        results
                );
                if (results[0] < radiusMeters) {
                    return true; // 危険エリアを通過
                }
            }
        }
        return false;
    }


    //ルート計算関数(計算自体はGoogleAPIなのでHTTP通信するためのロジック)
    private void fetchRoute(LatLng origin, LatLng destination) {

//APIへのURL作成
        String url = "https://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + origin.latitude + "," + origin.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude
                + "&mode=walking"
                + "&alternatives=false"
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

                Log.d("RouteFetch", "JSON: " + sb.toString());

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




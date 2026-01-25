package com.example.sosbaton;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.view.Menu;
import android.view.MenuItem;

import java.net.URLEncoder;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.location.LocationRequest;
import android.app.AlertDialog;
import java.util.List;
import java.util.ArrayList;
import android.location.Location;
import android.widget.Toast;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.Timestamp;
import android.view.Gravity;
import androidx.core.widget.NestedScrollView;
import com.google.android.gms.maps.model.LatLngBounds;
import android.os.Handler;
import android.os.Looper;
import android.widget.RadioGroup;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.content.Context;
import com.google.android.gms.maps.model.BitmapDescriptor;
import android.view.LayoutInflater;
import java.util.Iterator;
import com.google.firebase.firestore.WriteBatch;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import android.animation.ValueAnimator;
import com.google.firebase.firestore.SetOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot; // これも必要です

import org.json.JSONArray;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private String userName = "ゲスト";
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private MapView mapView;
    private static final String TAG = "Firestore";

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private List<LatLng> evacuationPoints = new ArrayList<>();
    private List<Marker> allMarkers = new ArrayList<>();

    private Marker myMarker;



    private Marker areaMarker;
//    private LocationCallback locationCallback;

    private FirebaseFirestore db;

    private FirebaseAuth auth;

    private FirebaseUser currentUser;

    private boolean isEvacuationRouteRequested = false;
    private final Object routeLock = new Object(); // スレッド安全のため
    private Marker selectedMarker = null;
    private String selectedDocId = null;
    //経路リスト
    private List<com.google.android.gms.maps.model.Polyline> currentPolylines = new ArrayList<>();

    //現在地座標
    private LatLng current;
    //避難所座標
    private LatLng position;
    //選択ピン保存用
     // 現在選択中の避難所ピン(docID)
    private String selectedshelterPinDocId = null;
     //現在選択中の避難所ピン(name)
    private String selectedshelterPinname = null;
    //現在選択中の避難所ピン(座標)
    LatLng selectedshelterPinlatlng = null;


    private boolean listenerRegistered = false;




    private NestedScrollView nestedScrollView;

    //検索回数制限用(避難所数）
    private int retryCount = 0;

    //検索回数制限用(リクエスト数)
    private int requestcount = 0;

    //再帰用フラグ
    private boolean isProcessingRoute = false;

    //避難所ピン描画切り替え用フラグ
    private boolean isProcessingShelterpin = false;






    //sosピン管理リスト(現ユーザ、ユーザID)
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String myuid;
    //sospinは一本
    List<Sospin> mySosPins = new ArrayList<>();




    //sosピン管理リスト
    private Map<String, Sospin> sosMarkerMap = new HashMap<>();


    private  GroundOverlay overlay;
    // 避難所キャッシュ
    private final List<Shelter> shelterCache = new ArrayList<>();

    //避難所ルート探索削除用リスト
    private   List<Shelter> shelterdelete = new ArrayList<>();

    //避難所保持用リスト
    private   List<Shelter> shelters = new ArrayList<>();

    // 表示中マーカー
    private final List<Marker> shelterMarkers = new ArrayList<>();
    private static final double CACHE_RADIUS_KM = 2.0; // 5km取得
    private LatLng lastCacheCenter = null; // 前回取得した範囲の中心
    private static final float CACHE_REFRESH_THRESHOLD = 1000f; // 200m 未満なら再取得しない

    private LatLng lastLatLng = null;
    boolean cameraInitialized = false;











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



        //避難ボタン
        ImageButton btn_post = findViewById(R.id.btn_post);
        //sosピンボタン
        ImageButton btn_pin = findViewById(R.id.btn_pin);

        //避難ボタンのクリック時
        btn_post.setOnClickListener(v->{
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("ルート選択")
                    .setMessage("避難方法を選択してください")
                    .setPositiveButton("危険回避ルート", (dialog, which) -> {
                        clearAllPolylines();
                        retryCount=0;
                        //避難所リスト再構築
                        shelterdelete.addAll(shelters);
                        isEvacuationRouteRequested = true;
                        loadEvacuationPointsFromDB();

                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,20));
                    })
                    .setNeutralButton("最短距離の避難所", (dialog, which) -> {
                        clearAllPolylines();
                        //描画用フラグオン
                        isProcessingShelterpin=true;
                        //とりあえず避難所ピン全消し(描画のみ)
                        for (Marker marker : shelterMarkers) {
                            marker.remove(); // 地図から消す
                        }
                        shelterMarkers.clear();






                        Shelter nearest = findNearestShelter();
                        if (nearest == null) {
                            Toast.makeText(this, "近くに避難所が見つかりません", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        LatLng target = new LatLng(nearest.lat, nearest.lng);

                        //ヒットした避難所ピンのみ描画
                        Marker marker = googleMap.addMarker(
                                new MarkerOptions()
                                        .position(target)
                                        .title(nearest.name )
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        );
                        if(marker!=null){
                            marker.setTag(nearest);
                        }


                        // 最短ルートを描画
                        drawRouteShortest(target);


//                        // カメラ移動
//                        LatLngBounds bounds = new LatLngBounds.Builder()
//                                .include(current)
//                                .include(selectedMarker.getPosition())
//                                .build();
//
//                        googleMap.animateCamera(
//                                CameraUpdateFactory.newLatLngBounds(bounds, 200)
//                        );

                        googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(current, 18f)
                        );
                        // 例: 最短ルート
                        /*drawRouteShortest(new LatLng(37.39830881, 140.35796203));
                        drawRouteShortest(new LatLng(37.376782, 140.392777));     // 東部体育館
                        drawRouteShortest(new LatLng(37.36942367, 140.37393403)); // ビッグパレット
                        drawRouteShortest(new LatLng(37.419631, 140.390504));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,20));// 富久山公民館*/
                    })
                    .setNegativeButton("ルートリセット", (dialog, which) -> {
                        clearAllPolylines();
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 18f)
                      );

                    })
                    .show();
        });


        //ピンボタンクリック時
        btn_pin.setOnClickListener(v->{

            if (user == null) {
                Toast.makeText(this,"ログインしてください",Toast.LENGTH_SHORT).show();
                return;
            }


            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.dialog_sos_question, null);

            RadioGroup rg1 = view.findViewById(R.id.radioGroup1);
            RadioGroup rg2 = view.findViewById(R.id.radioGroup2);
            RadioGroup rg3 = view.findViewById(R.id.radioGroup3);

            new AlertDialog.Builder(this)
                    .setTitle("救助要請")
                    .setView(view)
                    .setPositiveButton("確定", (dialog, which) -> {
                        if (rg1.getCheckedRadioButtonId() == -1 ||
                                rg2.getCheckedRadioButtonId() == -1 ||
                                rg3.getCheckedRadioButtonId() == -1) {

                            Toast.makeText(this, "すべての質問に回答してください", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int q1 = rg1.indexOfChild(view.findViewById(rg1.getCheckedRadioButtonId()))+1;
                        int q2 = rg2.indexOfChild(view.findViewById(rg2.getCheckedRadioButtonId()))+1;
                        int q3 = rg3.indexOfChild(view.findViewById(rg3.getCheckedRadioButtonId()))+1;

                        // ここで回答結果をまとめて扱える
                        // 例：Firestoreへ保存、pinType算出など
                        Log.d("SOS", "Q1=" + q1 + " Q2=" + q2 + " Q3=" + q3);



                        db.collection("sospin")
                                .whereEqualTo("uid", myuid)
                                .get()
                                .addOnSuccessListener(query -> {

                                    // ① 完全新規なら削除処理を通さずそのまま保存
                                    if (query.isEmpty()) {
                                        sosaddPin(current,3,q1,q2,q3,myuid);
                                        return;
                                    }

                                    // ② 既存ピンがある場合のみ削除処理







                                    WriteBatch batch = db.batch();
                                    for (DocumentSnapshot doc : query) {
                                        batch.delete(doc.getReference());
                                    }

                                    batch.commit()
                                            .addOnSuccessListener(aVoid -> {
                                                sosaddPin(current,3,q1,q2,q3,myuid);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "既存ピンの削除に失敗しました", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "通信エラーが発生しました", Toast.LENGTH_SHORT).show();
                                });

                    })
                    .setNegativeButton("キャンセル", null)
                    .show();
        });
        //ボトムシートボタン定義＆その他ボタン定義
        Button btngo = findViewById(R.id.btngo);
        Button btndelete = findViewById(R.id.btndelete);
        Button Close = findViewById(R.id.Close);
        Button back = findViewById(R.id.btnback);
        Button btncurrent = findViewById(R.id.btncurrent);
        Button btnchat =findViewById(R.id.btnchat);

        //閉じる
        Close.setOnClickListener(v->{
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });
        back.setOnClickListener(v->{
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });
        //現在地に戻る
        btncurrent.setOnClickListener(v->{

            if(current == null){
                return;
            }
            else{
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(current, 15)
                );
                myMarker.showInfoWindow();
            }

        });


        //ココへ行く
        btngo.setOnClickListener(v -> {
            clearAllPolylines();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            drawRouteShortest(selectedMarker.getPosition());
            LatLngBounds bounds = new LatLngBounds.Builder()
                    .include(current)
                    .include(selectedMarker.getPosition())
                    .build();

            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds, 100)
            );
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(current, 15)
                );
            }, 3000);


        });
        //削除
        btndelete.setOnClickListener(v->{

            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("ピン削除")
                    .setMessage("本当にこのピンを削除しますか？")
                    .setPositiveButton("削除", (dialog, which) -> {

                        deletePin(selectedMarker, selectedDocId);

                    })
                    .setNegativeButton("キャンセル", (dialog, which) -> dialog.dismiss())
                    .show();


        });


        setupBottomSheet();




// 起動時にログインユーザーをチェックする

        currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            // ログイン状態が維持されている

            saveFcmTokenToFirestore();

            myuid = user.getUid();

            // 現在のユーザー名（displayName）をチェックする
            String displayName = currentUser.getDisplayName();
            userName = displayName;

            db.collection("users")
                    .document(myuid)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() { // ← 型を明示
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) { // ← ここで変数を定義
                            if (documentSnapshot.exists()) {
                                // 1. userIdフィールドが存在するか確認
                                if (!documentSnapshot.contains("userId")) {
                                    // フィールドがない場合は作成（マージ）
                                    Map<String, Object> updateData = new HashMap<>();
                                    updateData.put("userId", myuid);

                                    db.collection("users").document(myuid)
                                            .set(updateData, SetOptions.merge());
                                }


                            } else {
                                // ドキュメント自体が存在しない場合
                                Log.d("Firestore", "No such document");
                            }

                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error getting document", e);
                    });





            if (displayName != null && !displayName.isEmpty()) {
                // ① displayNameが既に設定されている場合


                // マップ画面など、アプリのメインコンテンツを表示するのだ。

            } else {
                // ② displayNameが未設定の場合 (Firestoreからusernameを取得する)
                String currentUid = currentUser.getUid();

                // 独自にusernameを保存しているコレクション（例: "users"）にアクセスする
                db.collection("users")
                        .document(currentUid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {

                            if (documentSnapshot.exists()) {
                                String name = documentSnapshot.getString("username");

                                if(name!= null){
                                    userName = name;
                                }


                            }
                        })
                        .addOnFailureListener(e -> {
                            // Firestoreからの取得に失敗した場合
                            Log.e(TAG, "ユーザー名の取得に失敗しました: " + e.getMessage());
                            String welcomeMessage = "ようこそ、名無しさん！";
                            Toast.makeText(this, welcomeMessage, Toast.LENGTH_LONG).show();
                        });
            }

        } else {
            // 誰もログインしていない
            Log.d(TAG, "ログインが必要です。");
            Toast.makeText(this, "ゲストモードでは、一部機能の利用が制限されます。", Toast.LENGTH_LONG).show();
            // ログイン画面へ誘導する
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
                            String welcomeMessage = name + "さん、おかえりなさい！";
                            Toast.makeText(this, welcomeMessage, Toast.LENGTH_LONG).show();

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
        Menu menu = navigationView.getMenu();

        if (currentUser == null) {
            // 【ゲストモード：ログインを促す構成】
            menu.findItem(R.id.nav_home).setVisible(true).setTitle("ログイン・登録");
            menu.findItem(R.id.nav_settings).setVisible(false); // ログアウトは不要

            // ログインが必要な機能は隠しちゃうのだ！
            menu.findItem(R.id.nav_profile).setVisible(false);
            menu.findItem(R.id.nav_friend).setVisible(false);
            menu.findItem(R.id.nav_massage).setVisible(false);
        } else {
            // 【ログイン済み：フル機能解放】
            menu.findItem(R.id.nav_home).setVisible(false); // すでにログインしてるから不要
            menu.findItem(R.id.nav_settings).setVisible(true).setTitle("ログアウト");

            // 全機能を表示するのだ！
            menu.findItem(R.id.nav_profile).setVisible(true);
            menu.findItem(R.id.nav_friend).setVisible(true);
            menu.findItem(R.id.nav_massage).setVisible(true);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(MainActivity.this, StartActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_settings) {
                // 【ログアウト処理をここに追加するのだ！】
                auth.signOut(); // Firebaseからログアウトさせるのだ！

                // ログアウトが成功したら、スタート画面（またはログイン画面）に戻るのだ！
                // Intentのフラグを使って、現在開いているActivityをすべて閉じるのが確実なのだ。
                Intent intent = new Intent(MainActivity.this, StartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (id == R.id.nav_friend) {

                if (myuid != null) {
                    startActivity(new Intent(MainActivity.this, friendActivity.class));
                }else{
                    Toast.makeText(this, "ログインしてください", Toast.LENGTH_SHORT).show();
                }

            } else if (id == R.id.nav_massage) {
                startActivity(new Intent(MainActivity.this, FriendmsgActivity.class));
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
            //loadEvacuationPointsFromDB();


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





        //ボトムシートのナビゲーションバー対策
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.bottomSheet),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            v.getPaddingLeft(),
                            v.getPaddingTop(),
                            v.getPaddingRight(),
                            systemBars.bottom
                    );

                    // ScrollView 内の子コンテンツのpaddingも同じように bottom を加算
                    nestedScrollView.setPadding(
                            nestedScrollView.getPaddingLeft(),
                            nestedScrollView.getPaddingTop(),
                            nestedScrollView.getPaddingRight(),
                            systemBars.bottom
                    );

                    return insets;
                }
        );


        //掲示板
        btnchat.setOnClickListener(v->{

            if (user == null) {
                Toast.makeText(this,"ログインしてください",Toast.LENGTH_SHORT).show();
                return;
            }


            if (selectedshelterPinDocId == null) {
                Log.d(TAG, "変数になんも入っていないね");
                return;
            }
            Intent intent = new Intent(MainActivity.this, BulletinboardActivity.class);
            intent.putExtra("PIN_DOC_ID", selectedshelterPinDocId);
            intent.putExtra("PIN_NAME",selectedshelterPinname);
            intent.putExtra("my_user_name",userName);
            intent.putExtra("PIN_LAT_LNG",selectedshelterPinlatlng);
            startActivity(intent);
        });




    }

    private void loadEvacuationPointsFromDB() {
        db.collection("test_shelters")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    evacuationPoints.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");
                        String name = doc.getString("name");
                        String address = doc.getString("address");
                        String type = doc.getString("type");
                        String id = doc.getId();

                        if (lat != null && lng != null) {
                            LatLng point = new LatLng(lat, lng);
                           // evacuationPoints.add(point);


//                            shelterdelete.add(new Shelter(
//                                    id, name, address, type, lat, lng
//                            ));
//
//                            shelters.add(new Shelter(
//                                    id, name, address, type, lat, lng
//                            ));


                        }
                    }


                    // 🔹 フラグが立っていれば危険回避ルート描画
                    if (isEvacuationRouteRequested) {

                        // 作成したリストがnullでなければ問題なくルート探索へ移行
                        if (shelterdelete != null) {


                                // 引数を座標(LatLng)ではなく、Shelter(nearest)に変更
                                drawRouteAvoiding(shelterdelete);


                        } else {
                            Log.d("Navi", "候補となる避難所がリストにありません。");
                        }

                        // ここで false にすると、失敗した時の「再試行」が止まってしまう可能性があるため
                        // 成功したことが確定するまでフラグ管理は慎重に行う必要があります
                        isEvacuationRouteRequested = false;
                    }

                })
                .addOnFailureListener(e -> Log.e(TAG, "避難所読み込み失敗", e));
    }

    // 2点間の距離（メートル）を計算する
    private float distanceMeters(LatLng a, LatLng b) {
        float[] results = new float[1];
        Location.distanceBetween(
                a.latitude, a.longitude,
                b.latitude, b.longitude,
                results
        );
        return results[0];   // メートル
    }
    private void updateShelterMarkers() {
        Log.d("MAP", "updateShelterMarkers called");

        if (current == null || googleMap == null) return;

        // 🧹 既存マーカー削除
        if (!shelterMarkers.isEmpty()) {
            for (Marker m : shelterMarkers) {
                m.remove();
            }
            shelterMarkers.clear();
        }

        for (Shelter shelter : shelterCache) {

            LatLng shelterPos = new LatLng(shelter.lat, shelter.lng);
            float distance = distanceMeters(current, shelterPos);

            if (distance > 2000f) continue;

            Marker marker = googleMap.addMarker(
                    new MarkerOptions()
                            .position(shelterPos)
                            .title(shelter.name + " (" + (int)distance + "m)")
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            );
            marker.setTag(shelter);
            shelterMarkers.add(marker);
        }
    }

    // ----------------------------------------------------------------------
// 【補足：displayNameを更新する関数を別途作成する】

    private void updateFirebaseDisplayName(FirebaseUser user, String newDisplayName) {

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Log.d("Profile", "displayNameをusernameに更新完了");

                        userName = newDisplayName;

                        // 更新完了後、ユーザー名でToast表示する
                        Toast.makeText(this, "ようこそ、" + newDisplayName + "なのだ！", Toast.LENGTH_LONG).show();
                    } else {
                        Log.w("Profile", "更新失敗", task.getException());

                        userName = newDisplayName;

                        // 失敗した場合も、取得したusernameでとりあえずToast表示するのも手
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
                        current = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 18));
                        Log.d(TAG, "現在地取得成功: " + location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        Log.d(TAG, "現在地が取得できませんでした");
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map; // ★ ここで一度設定すれば十分なのだ
        loadSospin();//sosピンをロード

        /*if (!listenerRegistered) {
            startPinsListener();
            listenerRegistered = true;
        }*/

        // --- タップでメニュー表示 ---
        googleMap.setOnMapClickListener(latLng -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("ここで何をする？")
                    .setItems(new CharSequence[]{"赤ピン", "緑ピン", "ここへ行く", "キャンセル"},
                            (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        // type=1L (赤ピン)
                                        addPin(latLng, userName, 1);

                                        break;

                                    case 1:
                                        // type=2L (緑ピン)
                                        addPin(latLng, userName, 2);

                                        break;

                                    case 2:
                                        clearAllPolylines();
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

        // 以前あったevacuationPoints/evacuationNamesの同期的なマーカー作成ループは、
        // loadShelters()と重複・競合するため削除したのだ。
        // loadShelters()が避難所マーカーを作成するのだ。

        // --- Firestore 読み込み ---
        loadPinsFromFirestore(); // ★ ピンをロード。一度の呼び出しで十分なのだ。

        /* --- マーカークリックメニュー ---*/
        googleMap.setOnMarkerClickListener(marker -> {
            selectedMarker = marker;
            saveSelectedDocId(marker);
            Object tag = marker.getTag();


            if (tag instanceof Shelter) {//避難所ピン
                Shelter s = (Shelter) tag;
                 selectedshelterPinDocId = s.docId;
                 selectedshelterPinname  = s.name;
                 selectedshelterPinlatlng = new LatLng(s.lat,s.lng);

                //テキスト変更箇所
                txtTitle.setText("避難所情報");
                txtName.setText("場所:　"+s.name);
                txtAddress.setText("住所:　"+s.address);
                txtType.setText(s.type);
                //カメラズーム
                //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,15));
                //ボトムシート展開
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                //表示要素
                txtName.setVisibility(View.VISIBLE);
                txtAddress.setVisibility(View.VISIBLE);
                txtType.setVisibility(View.VISIBLE);
                //非表示要素
                Button btndelete = findViewById(R.id.btndelete);
                btndelete.setVisibility(View.GONE);
                txttime.setVisibility(View.GONE);
                txturgency.setVisibility(View.GONE);
                txtsosCategory.setVisibility(View.GONE);
                txtsupporttype.setVisibility(View.GONE);


            } else if (tag instanceof PinInfo) {//赤緑ピン
                PinInfo info = (PinInfo) tag;
                //テキスト変更箇所
                txtName.setText("投稿者:　"+info.name);
                txtTitle.setText("ピン情報");
                txtType.setText(info.typeName);
                //現状は住所の代わりに座標
                txtAddress.setText("座標:　"+String.format("Lat: %.5f, Lng: %.5f", info.lat, info.lng));
                //カメラズーム
                LatLng pin = new LatLng(info.lat, info.lng);

                 //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pin,20));
                //ボトムシート展開
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                //表示要素
                Button btndelete = findViewById(R.id.btndelete);
                btndelete.setVisibility(View.VISIBLE);
                txtName.setVisibility(View.VISIBLE);
                txtAddress.setVisibility(View.VISIBLE);
                txtType.setVisibility(View.VISIBLE);
                //非表示要素
                Button btnchat = findViewById(R.id.btnchat);
                btnchat.setVisibility(View.GONE);
                txttime.setVisibility(View.GONE);
                txturgency.setVisibility(View.GONE);
                txtsosCategory.setVisibility(View.GONE);
                txtsupporttype.setVisibility(View.GONE);

            } else if (tag instanceof Sospin) {//sosピン
                Sospin sos = (Sospin) tag;
                //テキスト変更箇所
                updateTimeAgo(sos.createdAt, txttime);
                txtName.setText("投稿者:　"+sos.Uname);
                txtsupporttype.setText(
                        sos.supporttype == 1L ? "対応:　  してほしい":
                                sos.supporttype == 2L ? "対応:　  いらない" :
                                        sos.supporttype == 3L ? "対応:　  第三者協力要請":
                                                "不明"
                );
                txtsosCategory.setText(
                        sos.sosCategory == 1L ? "状況: 　 体調不良" :
                                sos.sosCategory == 2L ? "状況: 　 事故" :
                                        sos.sosCategory == 3L ? "状況: 　 不審者":
                                                "不明"
                );
                txturgency.setText(
                        sos.urgency == 1L ?"ピン:　 未対応" :
                                sos.urgency == 2L ? "ピン:　 対応中" :
                                        sos.urgency == 3L ? "ピン:　 対応済み":
                                                "不明"
                );
                txtTitle.setText("sos情報");
                //カメラズーム
                //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,20));
                //ボトムシート展開
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                //表示要素
                txturgency.setVisibility(View.VISIBLE);
                txtsosCategory.setVisibility(View.VISIBLE);
                txtsupporttype.setVisibility(View.VISIBLE);
                txtName.setVisibility(View.VISIBLE);
                //非表示要素
                Button btndelete = findViewById(R.id.btndelete);
                btndelete.setVisibility(View.GONE);
                txtAddress.setVisibility(View.GONE);
                txtType.setVisibility(View.GONE);
                txttime.setVisibility(View.VISIBLE);

            }

            return false; // InfoWindow を開きたい場合 (ここは変えないのだ)
        });

        googleMap.setOnInfoWindowClickListener(marker -> {
            // 何か処理をするならここに書くのだ


        });


        // --- 権限あるなら位置更新 ---
        // ★ 冗長な記述を削除し、一度だけ実行するのだ
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(
            Context context,
            int vectorResId
    ) {
        Drawable drawable = ContextCompat.getDrawable(context, vectorResId);
        if (drawable == null) return null;

        int size = 100;

        drawable.setBounds(
                0,
                0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight()
        );

        Bitmap bitmap = Bitmap.createBitmap(
                size,
                size,
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /*private void startPinsListener() {

        db.collection("sospin")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null || googleMap == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {

                        if (dc.getType() == DocumentChange.Type.ADDED) {

                            DocumentSnapshot doc = dc.getDocument();

                            Double lat = doc.getDouble("lat_x");
                            Double lng = doc.getDouble("lng_y");
                            String name = doc.getString("name");

                            if (lat == null || lng == null) continue;

                            googleMap.addMarker(
                                    new MarkerOptions()
                                            .position(new LatLng(lat, lng))
                                            .title(name)
                            );
                        }
                    }
                });
    }*/


    private void addPin(LatLng pos, String userName, long type) {

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

                    areaMarker = googleMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(type == 1 ? "赤ピン" : "緑ピン")
                            .icon(BitmapDescriptorFactory.defaultMarker(color))
                    );
                    allMarkers.add(areaMarker);
                    areaMarker.showInfoWindow();



                    if (areaMarker != null) {
                        // type は String でも int でも OK（必要に応じて統一）
                        String typeName = (type == 1) ? "危険エリア（赤ピン）" : "安全エリア（緑ピン）";

                        PinInfo info = new PinInfo(
                                docRef.getId(), // docId
                                typeName,
                                userName,
                                type, // type にピンの種類を代入
                                pos.latitude,
                                pos.longitude
                        );

                        areaMarker.setTag(info);
                    }
                });
    }

    //一番近い避難所の位置を返す
    private Shelter findNearestShelter() {
        if (current == null || shelterCache.isEmpty()) return null;

        Shelter nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Shelter shelter : shelterCache) {
            LatLng pos = new LatLng(shelter.lat, shelter.lng);
            float distance = distanceMeters(current, pos);

            if (distance < minDistance) {
                minDistance = distance;
                nearest = shelter;
            }
        }
        return nearest;
    }


    //危険回避用のリストを使用した近い避難所を出す
    private Shelter findNearestShelter2() {
        if (current == null || shelterdelete.isEmpty()) return null;

        Shelter nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Shelter shelter : shelterdelete) {
            LatLng pos = new LatLng(shelter.lat, shelter.lng);
            float distance = distanceMeters(current, pos);

            if (distance < minDistance) {
                minDistance = distance;
                nearest = shelter;
            }
        }
        return nearest;
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
                        String docId = doc.getId(); // docIdを取得するのだ

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
                                    .title(type == 1 ? "危険エリア" : type == 2 ? "安全エリア" : "未設定ピン")
                                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                            );

                            if (marker != null) {
                                // type は String でも int でも OK（必要に応じて統一）
                                String typeName = (type != null && type == 1) ?
                                        "危険エリア（赤ピン）" : "安全エリア（緑ピン）";

                                // PinInfoクラスを使ってタグ付けをするのだ。
                                PinInfo info = new PinInfo(
                                        docId, // docId [cite: 135]
                                        typeName,
                                        name, // nameをPinInfoのnameに設定
                                        type, // type にピンの種類を代入
                                        lat,
                                        lng
                                );

                                marker.setTag(info); // PinInfoをタグとしてセット
                                allMarkers.add(marker); // 🔥 allMarkersにマーカーを追加するのだ
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
    //private boolean firstMoveCamera = true; // 初回カメラ移動用
    private com.google.android.gms.location.LocationCallback locationCallback =
            new com.google.android.gms.location.LocationCallback() {
                @Override
                public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                    if (locationResult == null) return;

                    Location location = locationResult.getLastLocation();
                    if (location == null) return;

                    // 📍 現在地を更新
                    current = new LatLng(
                            location.getLatitude(),
                            location.getLongitude()
                    );

                    if (myMarker == null) {
                        myMarker = googleMap.addMarker(
                                new MarkerOptions()
                                        .position(current)
                                        .title("現在地")
                                        .icon(bitmapDescriptorFromVector(MainActivity.this, R.drawable.person))
                                        .anchor(0.5f, 1.0f)
                                        .flat(true)
                        );
                    } else {
                        myMarker.setPosition(current);
                    }

                    /*// 初回だけカメラを現在地に移動
                    if (firstMoveCamera) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
                        firstMoveCamera = false;
                    }*/
// Firestore再取得判定（中心から200m以上移動したら再取得）
                    boolean needReload = false;
                    if (lastCacheCenter == null) {
                        needReload = true; // 初回は必ず取得
                    } else {
                        float distance = distanceMeters(lastCacheCenter, current); // m単位
                        if (distance >= CACHE_REFRESH_THRESHOLD) {
                            needReload = true;
                        }
                    }

                    if (needReload) {
                        loadSheltersCacheFromDB();

                        // 前回取得中心を更新
                        lastCacheCenter = current;
                    }


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

        // 【門番】すでに実行中なら、新しいリクエストを一切受け付けない
        if (isProcessingRoute) {
            Log.d(TAG, "計算中のため、新しいリクエストをスキップしました。");
            return;
        }

        // 【1. 門を閉める】これから重い処理（通信）を始める合図
        isProcessingRoute = true;

        // 直通ルートを取得
        new Thread(() -> {
            try {
                String urlDirect = "https://maps.googleapis.com/maps/api/directions/json?"
                        + "origin=" + origin.latitude + "," + origin.longitude
                        + "&destination=" + destination.latitude + "," + destination.longitude
                        + "&mode=walking"
                        + "&alternatives=false"
                        + "&key=" + BuildConfig.MAPS_API_KEY;

                JSONObject jsonObj = requestJson(urlDirect);
                if (jsonObj != null) {
                    JSONArray routes = jsonObj.getJSONArray("routes");
                    if (routes.length() > 0) {
                        String encoded = routes.getJSONObject(0)
                                .getJSONObject("overview_polyline").getString("points");
                        List<LatLng> points = decodePolyline(encoded);
                        if (!passesThroughDanger(points, dangerPins, 50)) {
                            // 安全なら直ちに描画して終了
                            runOnUiThread(() -> {
                                Polyline poly = googleMap.addPolyline(new PolylineOptions()
                                        .addAll(points)
                                        .width(12)
                                        .color(Color.MAGENTA)
                                        .geodesic(true)
                                );

                                currentPolylines.add(poly);
                                requestcount++;
                                Log.d(TAG, requestcount+"回目のリクエスト試行で完了しました。");
                                isProcessingRoute = false;
                                requestcount=0;

                                Log.d(TAG, "ルート発見。門を開放します。");
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
                if (tried >= maxTrials) {
                    isProcessingRoute = false;
                    requestcount=0;

                    Log.d(TAG, "上限値のため門を開放します。");
                    break;
                }
                tried++;
                requestcount++;
                Log.d(TAG, requestcount+"回目のリクエスト試行中です。");

                try {
                    // via: を使うことで必ずその地点を経由させる（経路を強制的に迂回させられる）
                    String waypointParam = "via:" + wp.latitude + "," + wp.longitude;
                    String url = "https://maps.googleapis.com/maps/api/directions/json?"
                            + "origin=" + origin.latitude + "," + origin.longitude
                            + "&destination=" + destination.latitude + "," + destination.longitude
                            + "&waypoints=" + URLEncoder.encode(waypointParam, "UTF-8")
                            + "&mode=walking"
                            + "&alternatives=false"
                            + "&key=" + BuildConfig.MAPS_API_KEY;

                    JSONObject jsonObj = requestJson(url);
                    if (jsonObj == null) continue;

                    JSONArray routes = jsonObj.getJSONArray("routes");
                    if (routes.length() == 0) continue;

                    String encoded = routes.getJSONObject(0)
                            .getJSONObject("overview_polyline").getString("points");
                    List<LatLng> points = decodePolyline(encoded);

                    // 返ってきたルートが危険ピンと被らなければ採用して終了
                    if (!passesThroughDanger(points, dangerPins, 50)) {
                        runOnUiThread(() -> {
                            Polyline poly = googleMap.addPolyline(new PolylineOptions()
                                    .addAll(points)
                                    .width(12)
                                    .color(Color.MAGENTA)
                                    .geodesic(true)
                            );
                            currentPolylines.add(poly);
                            Log.d(TAG, requestcount+"回リクエストを試行しました。");
                            requestcount=0;

                            isProcessingRoute = false;
                            Log.d(TAG, "ルート発見門を開放します。");
                        });

                        return;


                    }

                } catch (Exception e) {
                    Log.e("RouteAvoid", "候補試行で例外: ", e);
                }
            }

            /// 全部ダメだったら UI に失敗表示
            // 最後にすべてダメだった場合
            runOnUiThread(() -> {
                // 12方位全部試してダメだった（ここに来た）ということは、この避難所は「到達不能」
                Log.d(TAG, "次の避難所へ");
                retryCount++;
                Log.d(TAG, "避難所試行失敗。現在のretryCount: " + retryCount);

                // 4か所の避難所をルート探索する避難所数に設定
                if (retryCount >= 4) {
                    Log.d("RouteAvoid", "API試行回数が上限(15回)に達しました。");
                    Toast.makeText(this, "周辺の避難所に対して、安全なルートが見つかりません", Toast.LENGTH_LONG).show();
                    isProcessingRoute = false;
                    requestcount=0;

                    Log.d(TAG, "避難所の上限値のため門を開放します。");

                    return;
                }



                // 1. 今の避難所（リストの先頭）を削除
                if (shelterdelete != null && !(shelterdelete.size() <= 0)) {
                    shelterdelete.remove(0);

                    isProcessingRoute = false;

                    Log.d(TAG, "次の避難所移行のため門を開放します。");
                    // 2. まだリストに次の避難所があるなら、再トライ！
                    // これにより、自動的に「2番目に近い避難所」に対して12方位アタックが始まる
                    drawRouteAvoiding(shelterdelete);

                } else {
                    // 3. 全ての避難所リストを使い切ってもダメだった場合
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("避難不可")
                            .setMessage("全ての避難所へのルートが危険エリアにより遮断されています。周囲の安全を確保してください。")
                            .setPositiveButton("OK", null)
                            .show();

                    // カウンターなどをリセット
                    isProcessingRoute = false;
                    requestcount=0;

                    Log.d(TAG, "探索失敗のため門を開放します。");

                }
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

    private void drawRouteAvoiding(List<Shelter> shelterdelete) {





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


        // 2. 今のリストから「一番近い1件」を特定
        Shelter target = findNearestShelter2();
        if (target == null){
            return;
        }



        //オブジェクトから座標を取得
        LatLng destination = new LatLng(target.lat, target.lng);
        // dangerPinsを同期的に作る
        List<LatLng> dangerPins = new ArrayList<>();
        for (Marker m : allMarkers) { // allMarkers には Firestore からロードされたピンも含まれているはずなのだ。
            Object tag = m.getTag();
            if (tag instanceof PinInfo) { // PinInfoを持っているピンのみをチェックなのだ
                PinInfo info = (PinInfo) tag;

                // PinInfoのtypeを安全にLongとして取得するのだ
                Object raw = info.getType();
                Long type = null;
                if (raw instanceof Number) {
                    type = ((Number) raw).longValue(); // NumberであればlongValueで統一するのだ
                }

                // もし赤ピン（type == 1L）ならば、危険エリアに追加するのだ
                if (type != null && type == 1L) { // 赤ピンのIDが1であるという前提なのだ
                    dangerPins.add(m.getPosition());
                    Log.d("RouteAvoid", "危険ピンを検出したのだ: " + m.getPosition());
                }
            }
            // 注意: loadSheltersでセットされるShelterタグは危険ピンの対象外と仮定するのだ
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

                        //実際は12方位の中継箇所に試行して無理なら終わるので実質直線を含めた13回の試行回数になる
                        tryRouteDirectThenCandidates(origin, destination, allCandidates, 15, dangerPins);
                    }
                });
    }

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
                com.google.android.gms.maps.model.Polyline poly   = googleMap.addPolyline(new com.google.android.gms.maps.model.PolylineOptions()
                        .addAll(points)
                        .width(12)//←線の太さ
                        .color(android.graphics.Color.BLUE) // 線の色
                        .geodesic(true)//曲面に沿った自然な線にする
                );
                currentPolylines.add(poly);
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

        db.collection("pins").document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "ピン削除成功: " + docId))
                .addOnFailureListener(e -> Log.w(TAG, "ピン削除失敗", e));
        marker.remove();  // マップから削除
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
    //④ボトムシートの初期化処理

    //ボトムシートの開閉やスライド制御のインスタンス
    BottomSheetBehavior<View> bottomSheetBehavior;

    TextView txtName, txtAddress, txtType,txtTitle,txttime,txtsupporttype,txtsosCategory,txturgency;


    private void setupBottomSheet() {
        View bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setDraggable(true);
        //eventのやり取りができるスクロールコンテナ
        nestedScrollView = findViewById(R.id.shelterInfoScroll);
        nestedScrollView.setNestedScrollingEnabled(true);
        //スクロールバー
        nestedScrollView.setVerticalScrollBarEnabled(true); // 表示可能
        nestedScrollView.setScrollbarFadingEnabled(false);    // フェードさせず常に表示
        //表示要素
        txtName = findViewById(R.id.txtShelterName);
        txtAddress = findViewById(R.id.txtShelterAddress);
        txtType = findViewById(R.id.txtShelterType);
        txtTitle = findViewById(R.id.txtTitle);
        //最初は非表示(ボトムシート)
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //sosのみ
        txttime = findViewById(R.id.txttime);
        txturgency = findViewById(R.id.txturgency);
        txtsosCategory = findViewById(R.id.txtsosCategory);
        txtsupporttype = findViewById(R.id.txtsupporttype);

        //event権限の分け合うロジック
        nestedScrollView.setOnScrollChangeListener(
                new NestedScrollView.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(
                            NestedScrollView v,
                            int scrollX,
                            int scrollY,
                            int oldScrollX,
                            int oldScrollY
                    ) {
                        if (!v.canScrollVertically(-1)) {
                            // 一番上
                            bottomSheetBehavior.setDraggable(true);
                        } else {
                            // スクロール中
                            bottomSheetBehavior.setDraggable(false);
                        }
                    }
                }
        );


    }

    //選択しているピンを認識するための関数
    private void saveSelectedDocId(Marker marker) {
        Object tag = marker.getTag();

        if (tag instanceof Shelter) {
            Shelter s = (Shelter) tag;
            selectedDocId = s.docId;
            Log.d("TAG", "Shelter docId を保存: " + selectedDocId);
            return;
        }

        if (tag instanceof PinInfo) {
            PinInfo p = (PinInfo) tag;
            selectedDocId = p.docId;
            Log.d("TAG", "Shelter docId を保存: " + selectedDocId);
            return;
        }


        selectedDocId = null;
        Log.w("TAG", "docId を保存できませんでした（tag が不明）");
    }

    private void loadSheltersCacheFromDB() {
        if (current == null) return;

        double lat = current.latitude;
        double lng = current.longitude;

        // 緯度・経度の矩形範囲を計算（半径2km）
        double latDelta = CACHE_RADIUS_KM / 111.0;
        double lngDelta = CACHE_RADIUS_KM / (111.0 * Math.cos(Math.toRadians(lat)));

        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLng = lng - lngDelta;
        double maxLng = lng + lngDelta;

        Log.d("MAP", "Firestore 範囲検索: "
                + minLat + "〜" + maxLat + ", "
                + minLng + "〜" + maxLng);

        db.collection("shelters")
                .whereGreaterThanOrEqualTo("lat", minLat)
                .whereLessThanOrEqualTo("lat", maxLat)
                .get()
                .addOnSuccessListener(query -> {

                    shelterCache.clear();

                    for (DocumentSnapshot doc : query) {

                        Double sLat = doc.getDouble("lat");
                        Double sLng = doc.getDouble("lng");
                        if (sLat == null || sLng == null) continue;

                        // 経度で最終フィルタ
                        if (sLng < minLng || sLng > maxLng) continue;

                        String id = doc.getId();
                        String name = doc.getString("name");
                        String address = doc.getString("address");
                        String type = doc.getString("type");


//                        Marker marker = googleMap.addMarker(new MarkerOptions()
//                                .position(new LatLng(sLat, sLng))
//                                .title(name));

                        shelterCache.add(new Shelter(
                                id, name, address, type, sLat, sLng
                        ));

                        shelterdelete.add(new Shelter(
                                id, name, address, type, sLat, sLng
                        ));

                        shelters.add(new Shelter(
                                id, name, address, type, sLat, sLng
                        ));

//                     避難所用保持リスト
//                      shelterMarkers.add(marker);

                    }

                    Log.d("MAP", "キャッシュ取得完了: " + shelterCache.size() + "件");

                    // キャッシュからピン表示
                    updateShelterMarkers();
                });
    }

    // ルート削除（複数可）関数
    private void clearAllPolylines() {
        for (Polyline p : currentPolylines) {
            p.remove(); // 地図から削除
        }
        currentPolylines.clear(); // リストもクリア
        if(isProcessingShelterpin==true){
            // 2. リストの中身を一つずつ取り出して描画
            for (Shelter shelter : shelterCache) {
                // 座標を作成
                LatLng shelterPos = new LatLng(shelter.lat, shelter.lng);

                // 距離を計算（現在地からの場合）
                // ※ すでに計算済みの distance 変数がある前提
                float[] results = new float[1];
                Location.distanceBetween(current.latitude, current.longitude, shelter.lat, shelter.lng, results);
                int distance = (int) results[0];

                // マーカーを描画
                Marker marker = googleMap.addMarker(
                        new MarkerOptions()
                                .position(shelterPos)
                                .title(shelter.name + " (" + distance + "m)")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                );

                // 管理用リストに追加
                shelterMarkers.add(marker);


                if (marker != null) {
                    marker.setTag(shelter);
                } else  {
                    Log.d(TAG, "shelterがnull");
                }

            }

        }
        isProcessingShelterpin=false;
    }


    //ファイヤベースのsosピン情報を取得
    //避難所ピン情報をファイヤベースから取得
    private void loadSospin() {
        db.collection("sospin").get().addOnSuccessListener(query -> {
            for (DocumentSnapshot doc : query) {
                String docId = doc.getId();
                //流用する場合は型変換
                Long pinTypeLong = doc.getLong("pinType");
                Long sosCategoryLong= doc.getLong("sosCategory");
                Long urgencyLong = doc.getLong("urgency");
                Long supporttypeLong = doc.getLong("supporttype");
                Double lat = doc.getDouble("lat");
                Double lng = doc.getDouble("lng");
                String name = doc.getString("name");
                Timestamp timestamp = doc.getTimestamp("createdAt");
                LatLng sosposition = new LatLng(lat, lng);
                String uid = doc.getString("uid");


                // nullチェック（超重要）
                if (pinTypeLong == null || lat == null || lng == null || timestamp == null) {
                    Log.d("SosPinLoad", "失敗でやんす");
                    if(pinTypeLong==null){
                        Log.d("SosPinLoad1", "pinTypeLongでやんす");
                        return;
                    }
                    if(lat == null){
                        Log.d("SosPinLoad1", "latでやんす");
                        return;
                    }
                    if(lng == null){
                        Log.d("SosPinLoad1", "lngでやんす");
                        return;
                    }
                    if(timestamp==null){
                        Log.d("SosPinLoad1", "timestampでやんす");
                        return;
                    }

                    Log.d("SosPinLoad1", "なんでか抜けたでやんす");
                    return;

                }

                // 型変換(これで型の一致やLong型の流用が可能に
                long type = pinTypeLong; // Long → long（アンボクシング）
                long sosCategory = sosCategoryLong;
                long urgency = urgencyLong;
                long supporttype = supporttypeLong;
                long createdAt = timestamp.toDate().getTime(); // Timestamp → long


                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(sosposition)
                        .title("SOS（救助要請）")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                Sospin sos = new Sospin(
                        type,
                        lat,
                        lng,
                        createdAt,
                        sosCategory,
                        urgency,
                        supporttype,
                        name,
                        uid,
                        docId
                );
                marker.setTag(sos);
                sos.marker = marker;
                mySosPins.add(sos);
            }

        });

    }



    //sosピン追加関数
    private void sosaddPin(LatLng pos, long type,int q1, int q2, int q3,String uid) {

        Map<String, Object> pinData = new HashMap<>();
        pinData.put("lat", pos.latitude);
        pinData.put("lng", pos.longitude);
        LatLng efect = new LatLng(pos.latitude,pos.longitude);
        Timestamp now = Timestamp.now();
        pinData.put("createdAt", now);
        long createdAtMillis = now.toDate().getTime();
        pinData.put("pinType", type);
        pinData.put("urgency", q1);
        pinData.put("sosCategory", q2);
        pinData.put("supporttype", q3);
        pinData.put("name",userName);
        pinData.put("uid", uid);





        db.collection("sospin")
                .document(uid)   // ← uid固定
                .set(pinData)
                .addOnSuccessListener(v -> {


                    // 既存ピンを全削除してから表示
                    clearMySosPinFromMap();



                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title("sos（救助要請）")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    );

                    if(q3==1){

                        GroundOverlayOptions options = new GroundOverlayOptions()
                                .image(BitmapDescriptorFactory.fromResource(R.drawable.efect5)) // 波紋画像
                                .position(efect, 10000f) // 半径100m
                                .transparency(0.5f);

                        overlay = googleMap.addGroundOverlay(options);

// 拡大アニメーション
                        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
                        animator.setDuration(5000);
                        animator.setRepeatCount(ValueAnimator.INFINITE);
                        animator.addUpdateListener(animation -> {
                            float value = (float) animation.getAnimatedValue();
                            overlay.setDimensions(10000 - value * 5000); // 徐々に拡大
                            overlay.setTransparency(0.5f - value * 0.5f); // 徐々に薄く
                        });
                        animator.start();

                    } else if (overlay != null) {
                        overlay.remove();
                    }


                    allMarkers.add(marker);
                    marker.showInfoWindow();




                    if (marker != null) {




                        Sospin sos = new Sospin(
                                type, // type にピンの種類を代入
                                pos.latitude,
                                pos.longitude,
                                createdAtMillis,
                                q1,
                                q2,
                                q3,
                                userName,
                                uid,
                                uid



                        );

                        marker.setTag(sos);
                        sos.marker = marker;
                        mySosPins.add(sos);
                    }

                    Toast toast = Toast.makeText(this, "救助要請に成功しました", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 120);
                    toast.show();

                })
                .addOnFailureListener(e -> {

                    Toast toast = Toast.makeText(this, "救助要請に失敗しました", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 120);
                    toast.show();

                    Log.e("SOS", "Firestore保存失敗", e);
                });

    }



    //時間変更関数
    public void updateTimeAgo(long createdAt, TextView txttime) {
        long now = System.currentTimeMillis();
        long diff = now - createdAt;

        long minutes = diff / (1000 * 60);
        long hours = diff / (1000 * 60 * 60);
        String timeAgo;
        if (minutes < 1) timeAgo = "たった今";
        else if (minutes < 60) timeAgo = minutes + "分前";
        else if (hours < 24) timeAgo = hours + "時間前";
        else timeAgo = (hours / 24) + "日前";

        txttime.setText("投稿日時:　"+timeAgo);
    }



    private void clearMySosPinFromMap() {
        for (Iterator<Sospin> it = mySosPins.iterator(); it.hasNext();) {
            Sospin s = it.next();
            if (s.uid.equals(myuid)) {

                db.collection("sospin").document(s.docId).delete();

                if (s.marker != null) s.marker.remove();
                it.remove();

            }
        }
    }


    private void saveFcmTokenToFirestore() {
        String myUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (myUid == null) return;

        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        android.util.Log.w("FCM", "トークンの取得に失敗しました", task.getException());
                        return;
                    }

                    // このデバイス固有の住所（トークン）
                    String token = task.getResult();

                    // Firestoreのユーザー情報に「fcmToken」として保存
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(myUid)
                            .update("fcmToken", token)
                            .addOnSuccessListener(aVoid -> android.util.Log.d("FCM", "トークン保存成功！"))
                            .addOnFailureListener(e -> android.util.Log.e("FCM", "トークン保存失敗", e));
                });
    }

}
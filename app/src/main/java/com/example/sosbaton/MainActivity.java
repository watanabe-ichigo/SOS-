package com.example.sosbaton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.MapView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private MapView mapView;

    private static final String TAG = "Firestore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Firestoreテスト開始");

        // EdgeToEdge の有効化
        EdgeToEdge.enable(this);

        // Firebase 初期化
        FirebaseApp.initializeApp(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 🔸 ここでレイアウトをセット（これが最初！）
        setContentView(R.layout.activity_main);

        // --- View の取得 ---
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);
        mapView = findViewById(R.id.mapView);

        // --- Toolbar を ActionBar にセット ---
        setSupportActionBar(toolbar);

        // --- ユーザー名を受け取る ---
        String userName = getIntent().getStringExtra("USER_NAME");

        // --- NavigationViewのヘッダーを取得 ---
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tvUserName);

        if (tvUserName != null) {
            if (userName != null && !userName.isEmpty()) {
                tvUserName.setText(userName + " さん");
            } else {
                tvUserName.setText("ログイン中ユーザー");
            }
        }

        // --- Firestoreテスト ---
        db.collection("users").document("user001")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if (document.exists()) {
                            String name = document.getString("name");
                            Long age = document.getLong("age");
                            String email = document.getString("email");

                            Log.d(TAG, "ユーザー名: " + name + ", 年齢: " + age + ", メール: " + email);
                        } else {
                            Log.d(TAG, "ドキュメントが存在しません");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.w(TAG, "データ取得に失敗しました", e);
                    }
                });

        // --- ハンバーガーアイコンで Drawer 開閉 ---
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // --- NavigationView のメニュークリック処理 ---
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_settings) {
                // 設定クリック時の処理
            }

            drawerLayout.closeDrawers();
            return true;
        });

        // --- WindowInsetsListener で EdgeToEdge 対応 ---
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- MapView 初期化 ---
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }

        // --- SOSボタン ---
        Button sosButton = findViewById(R.id.sosButton);
        if (sosButton != null) {
            sosButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SosActivity.class);
                startActivity(intent);
            });
        }
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
}

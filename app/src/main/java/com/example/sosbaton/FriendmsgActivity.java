package com.example.sosbaton;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sosbaton.adapter.FriendRequestAdapter;
import com.example.sosbaton.model.FriendRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class FriendmsgActivity extends AppCompatActivity {

    // --- UI ---
    RecyclerView rvMessageList;
    ImageButton btnClose;

    // --- RecyclerView ---
    FriendRequestAdapter adapter;
    List<FriendRequest> requestList = new ArrayList<>();

    // --- Firebase ---
    FirebaseFirestore db;
    String myUid;
    ListenerRegistration requestListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_massage);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // ステータスバーのアイコンを暗い色（黒など）に設定する
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }

        View rootLayout = findViewById(R.id.root_layout);
        if (rootLayout != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(
                        androidx.core.view.WindowInsetsCompat.Type.systemBars()
                );

                // XMLの android:padding="16dp" を考慮するわよ
                float density = getResources().getDisplayMetrics().density;
                int basePaddingPx = (int) (16 * density);

                v.setPadding(
                        systemBars.left + basePaddingPx,
                        systemBars.top + basePaddingPx,
                        systemBars.right + basePaddingPx,
                        systemBars.bottom + basePaddingPx
                );

                return insets;
            });
        }

        // UI取得
        btnClose = findViewById(R.id.btnClose);
        rvMessageList = findViewById(R.id.rvMessageList);

        // 閉じる
        btnClose.setOnClickListener(v -> finish());

        // RecyclerView初期化
        rvMessageList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendRequestAdapter(requestList, getMyUid());
        rvMessageList.setAdapter(adapter);

        // Firestore
        db = FirebaseFirestore.getInstance();
    }

    private String getMyUid() {
        return FirebaseAuth.getInstance().getUid();
    }

    // --------------------
    // 監視開始
    // --------------------
    @Override
    protected void onStart() {
        super.onStart();
        startListeningFriendRequests();
    }

    // --------------------
    // 監視停止（超重要）
    // --------------------
    @Override
    protected void onStop() {
        super.onStop();
        if (requestListener != null) {
            requestListener.remove();
            requestListener = null;
        }
    }

    // --------------------
    // フレンド申請監視
    // --------------------
    private void startListeningFriendRequests() {

        myUid = getMyUid();
        if (myUid == null) return;

        requestListener = db.collection("friend_requests")
                .whereEqualTo("to_id", myUid)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshots, e) -> {

                    for (DocumentSnapshot doc : snapshots) {
                        Log.d("DEBUG", doc.getData().toString());
                    }
                    if (e != null || snapshots == null) return;

                    requestList.clear();

                    for (DocumentSnapshot doc : snapshots) {
                        FriendRequest req = doc.toObject(FriendRequest.class);
                        if (req == null) continue;

                        req.setDocId(doc.getId());
                        requestList.add(req);
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}

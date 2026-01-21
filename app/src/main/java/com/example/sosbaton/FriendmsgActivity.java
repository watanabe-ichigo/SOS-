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
                .whereEqualTo("to", myUid)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshots, e) -> {

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

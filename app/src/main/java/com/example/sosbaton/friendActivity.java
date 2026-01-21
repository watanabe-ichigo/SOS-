package com.example.sosbaton;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class friendActivity extends AppCompatActivity {

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_friend);

        db = FirebaseFirestore.getInstance();

        ImageButton close = findViewById(R.id.btnClose);
        EditText etFriendUserId = findViewById(R.id.etFriendUserId);
        Button btnAddFriend = findViewById(R.id.btnAddFriend);

        btnAddFriend.setOnClickListener(v -> {

            String inputUid = etFriendUserId.getText().toString().trim();

            if (inputUid.isEmpty()) {
                Toast.makeText(this, "ユーザーIDを入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "ログインしてください", Toast.LENGTH_SHORT).show();
                return;
            }

            String myUid = user.getUid();

            if (inputUid.equals(myUid)) {
                Toast.makeText(this, "自分自身は追加できません", Toast.LENGTH_SHORT).show();
                return;
            }

            // ① すでにフレンド？
            checkAlreadyFriend(myUid, inputUid, isFriend -> {
                if (isFriend) {
                    Toast.makeText(this, "すでにフレンドです", Toast.LENGTH_SHORT).show();
                } else {
                    // ② すでに申請済み？
                    checkAlreadyRequested(myUid, inputUid, requested -> {
                        if (requested) {
                            Toast.makeText(this, "すでに申請しています", Toast.LENGTH_SHORT).show();
                        } else {
                            // ③ 申請送信（ここだけ）
                            sendFriendRequest(myUid, inputUid);
                        }
                    });
                }
            });
        });

        close.setOnClickListener(v -> finish());
    }

    // -----------------------
    // フレンド申請送信
    // -----------------------
    private void sendFriendRequest(String myUid, String friendUid) {

        Map<String, Object> request = new HashMap<>();
        request.put("from", myUid);
        request.put("to", friendUid);
        request.put("status", "pending");
        request.put("created_at", FieldValue.serverTimestamp());

        db.collection("friend_requests")
                .add(request)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "フレンド申請を送信しました", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "申請に失敗しました", Toast.LENGTH_SHORT).show();
                });
    }

    // -----------------------
    // すでに申請済みか
    // -----------------------
    private void checkAlreadyRequested(String myUid, String friendUid,
                                       Consumer<Boolean> callback) {

        db.collection("friend_requests")
                .whereEqualTo("from", myUid)
                .whereEqualTo("to", friendUid)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(qs -> {
                    callback.accept(!qs.isEmpty());
                });
    }

    // -----------------------
    // すでにフレンドか
    // -----------------------
    private void checkAlreadyFriend(String myUid, String friendUid,
                                    Consumer<Boolean> callback) {

        db.collection("users")
                .document(myUid)
                .collection("friend_list")
                .document(friendUid)
                .get()
                .addOnSuccessListener(doc -> {
                    callback.accept(doc.exists());
                });
    }
}

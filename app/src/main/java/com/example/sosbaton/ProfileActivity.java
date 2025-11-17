package com.example.sosbaton;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView tvUserNameTop, tvValueName, tvValueEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 戻るボタン
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // TextView を取得
        tvUserNameTop = findViewById(R.id.tvUserNameTop); // 上部のユーザー名
        tvValueName = findViewById(R.id.tvValueName);     // 名前欄
        tvValueEmail = findViewById(R.id.tvValueEmail);   // メール欄

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail(); // メール
            String uid = user.getUid();     // UID

            tvValueEmail.setText(email);

            // Firestoreから名前を取得
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists() && doc.contains("username")) {
                            String name = doc.getString("username");
                            tvUserNameTop.setText(name + " さん");
                            tvValueName.setText(name);
                        } else {
                            tvUserNameTop.setText("名無し さん");
                            tvValueName.setText("名無し");
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(ProfileActivity.this, "ユーザー情報取得失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}

package com.example.sosbaton;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.ImageView;
import android.widget.EditText;
import android.view.View;



public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView tvUserNameTop, tvValueName, tvValueEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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

        // ✏️ ここで編集ボタンも取得
        ImageView btnEditName = findViewById(R.id.btnEditName);
        btnEditName.setOnClickListener(v -> showEditNameDialog());

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
    private void showEditNameDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // ダイアログ作成
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("名前を変更");

        final EditText input = new EditText(this);
        input.setText(tvValueName.getText().toString());
        builder.setView(input);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String newName = input.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "新たなユーザーネームを入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firestore 更新
            db.collection("users").document(uid)
                    .update("username", newName)
                    .addOnSuccessListener(aVoid -> {
                        // UI 更新
                        tvValueName.setText(newName);
                        tvUserNameTop.setText(newName + " さん");
                        Toast.makeText(this, "更新しました", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "更新失敗 " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("キャンセル", (dialog, which) -> dialog.cancel());
        builder.show();
    }


}

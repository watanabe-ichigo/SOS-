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

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import android.util.Log;




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

        // 戻る
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // TextView
        tvUserNameTop = findViewById(R.id.tvUserNameTop);
        tvValueName = findViewById(R.id.tvValueName);
        tvValueEmail = findViewById(R.id.tvValueEmail);

        // Edit ボタン
        ImageView btnEditName = findViewById(R.id.btnEditName);
        ImageView btnEditEmail = findViewById(R.id.btnEditEmail);
        ImageView btnEditPassword = findViewById(R.id.btnEditPassword);

        btnEditName.setOnClickListener(v -> showEditNameDialog());
        btnEditEmail.setOnClickListener(v -> showEditEmailDialog());
        btnEditPassword.setOnClickListener(v -> showEditPasswordDialog());

        // ユーザー情報読み込み
        loadUserInfo();
    }

    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String email = user.getEmail();
        String uid = user.getUid();

        tvValueEmail.setText(email);

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("username")) {
                        String name = doc.getString("username");
                        tvUserNameTop.setText(name + " さん");
                        tvValueName.setText(name);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "ユーザー情報取得失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    // ① 名前変更ダイアログ
    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    private void showEditNameDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("名前を変更");

        final EditText input = new EditText(this);
        input.setText(tvValueName.getText().toString());
        builder.setView(input);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "名前を入力してね", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users").document(uid)
                    .update("username", newName)
                    .addOnSuccessListener(aVoid -> {
                        tvValueName.setText(newName);
                        tvUserNameTop.setText(newName + " さん");
                        Toast.makeText(this, "更新しました", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "更新失敗 " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("キャンセル", (d, w) -> d.cancel());
        builder.show();
    }

    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    // ② メール変更ダイアログ（再認証 + verifyBeforeUpdateEmail）
    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    private void showEditEmailDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("メールを変更");

        View view = getLayoutInflater().inflate(R.layout.dialog_change_email, null);
        EditText etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        EditText etNewEmail = view.findViewById(R.id.etNewEmail);

        builder.setView(view);

        builder.setPositiveButton("変更", (dialog, which) -> {

            String currentPassword = etCurrentPassword.getText().toString();
            String newEmail = etNewEmail.getText().toString().trim();

            if (currentPassword.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "必要な項目を入力してね", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔐 再認証
            AuthCredential credential = EmailAuthProvider
                    .getCredential(user.getEmail(), currentPassword);

            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {

                        // ★ Firebase 正攻法：まず確認メールを送らせる
                        user.verifyBeforeUpdateEmail(newEmail)
                                .addOnSuccessListener(v -> {

                                    // UI 上はとりあえず新しいメール表示だけ更新しておく
                                    tvValueEmail.setText(newEmail);

                                    Toast.makeText(this,
                                            "新しいメールに確認リンク送ったのだ。"
                                                    + "リンク踏んだらメール変更が確定するのだぞ！",
                                            Toast.LENGTH_LONG).show();

                                })
                                .addOnFailureListener(e -> {
                                    Log.e("EMAIL_UPDATE", "verifyBeforeUpdateEmail失敗", e);
                                    Toast.makeText(this,
                                            "メール送信失敗: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "パスワードが違います", Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("キャンセル", (d, w) -> d.cancel());
        builder.show();
    }




    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    // ③ パスワード変更ダイアログ（再認証付き）
    // ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
    private void showEditPasswordDialog() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("パスワード変更");

        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = view.findViewById(R.id.etNewPassword);

        builder.setView(view);

        builder.setPositiveButton("変更", (dialog, which) -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();

            if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "必要な項目を入力してね", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider
                    .getCredential(user.getEmail(), currentPassword);

            // 再認証
            user.reauthenticate(credential).addOnSuccessListener(aVoid -> {
                user.updatePassword(newPassword)
                        .addOnSuccessListener(v ->
                                Toast.makeText(this, "パスワードを変更しました", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "変更失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "現在のパスワードが違います", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("キャンセル", (d, w) -> d.cancel());
        builder.show();
    }
}

package com.example.sosbaton;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class StartActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        EditText etPassword = findViewById(R.id.etPassword);
        ImageButton btnTogglePassword = findViewById(R.id.btnTogglePassword);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View root = findViewById(R.id.root_layout); // ConstraintLayout の id
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 全方向に inset を padding として反映させたい場合
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return insets;
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // 現在のフラグを取得し、LIGHT_STATUS_BAR フラグを追加するのだ
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags |= android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }


        // パスワード表示切替
        btnTogglePassword.setOnClickListener(v -> {
            if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText etEmail = findViewById(R.id.etEmail);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        ImageButton backButton = findViewById(R.id.backButton);

        //戻るボタン処理
        backButton.setOnClickListener(v -> {
            // ゲストモードとして MainActivity を起動するのだ
            Intent intent = new Intent(StartActivity.this, MainActivity.class);

            // 大事：これまでのアクティビティ履歴をクリアして、
            // まっさらな状態で MainActivity を立ち上げるのだ！
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            // 自分（ログイン画面）はもう用済みだから消えるのだ！
            finish();
        });

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "メールとパスワードを入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔑 Firebase Authenticationでログイン
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();

                            if (user != null) {
                                // Firestore からユーザー情報を取得（名前など）
                                db.collection("users").document(user.getUid())
                                        .get()
                                        .addOnSuccessListener(doc -> {
                                            String name = doc.contains("name")
                                                    ? doc.getString("name")
                                                    : "名無し";

                                            // ログイン成功 → 地図画面へ
                                            Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                            intent.putExtra("USER_NAME", name);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "ユーザー情報取得失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }

                        } else {
                            Toast.makeText(this, "ログイン失敗：" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, RegisterActivity.class);
            startActivity(intent);
        });


    }
}
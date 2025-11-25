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

public class StartActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        EditText etPassword = findViewById(R.id.etPassword);
        ImageButton btnTogglePassword = findViewById(R.id.btnTogglePassword);


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
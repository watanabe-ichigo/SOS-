package com.example.sosbaton;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth; // 🔹 FirebaseAuth 追加

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance(); // 🔹 初期化

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);

        ImageButton btnTogglePassword = findViewById(R.id.btnTogglePassword);
        ImageButton btnToggleConfirm = findViewById(R.id.btnToggleConfirm);
        Button btnRegister = findViewById(R.id.btnRegister);
        ImageButton backButton = findViewById(R.id.backButton);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Activity の onCreate 内
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View root = findViewById(R.id.root_layoutn);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            // システムバーの inset を取得
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // 全方向に padding を反映
            v.setPadding(
                    systemBarsInsets.left,
                    systemBarsInsets.top,
                    systemBarsInsets.right,
                    systemBarsInsets.bottom
            );

            return insets;
        });


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // 現在のフラグを取得し、LIGHT_STATUS_BAR フラグを追加するのだ
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags |= android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        //戻るボタン処理
        backButton.setOnClickListener(v -> {
            finish();
        });

        // パスワード表示切替
        btnTogglePassword.setOnClickListener(v -> {
            if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        btnToggleConfirm.setOnClickListener(v -> {
            if (etConfirmPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });

        // 登録処理
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            String confirm = etConfirmPassword.getText().toString();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "全ての項目を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "パスワードが一致しません", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔹 Firebase Authentication に登録
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();

                                // 🔹 Firestore に UID をキーに保存
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("username", username);
                                userMap.put("email", email);
                                userMap.put("iconUrl", ""); // 後で設定可能

                                db.collection("users").document(uid)
                                        .set(userMap)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "登録完了しました", Toast.LENGTH_SHORT).show();
                                            finish(); // 登録完了後に戻る
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Firestore保存失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                                        );
                            }
                        } else {
                            Toast.makeText(this, "Auth登録失敗：" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}

package com.example.sosbaton;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);

        ImageButton btnTogglePassword = findViewById(R.id.btnTogglePassword);
        ImageButton btnToggleConfirm = findViewById(R.id.btnToggleConfirm);

        Button btnRegister = findViewById(R.id.btnRegister);

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

            Map<String, Object> user = new HashMap<>();
            user.put("name", username);
            user.put("email", email);
            user.put("password", password);

            db.collection("users")
                    .add(user)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "登録完了しました", Toast.LENGTH_SHORT).show();
                        finish(); // 登録完了後に前の画面に戻る
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "登録に失敗しました：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }
}
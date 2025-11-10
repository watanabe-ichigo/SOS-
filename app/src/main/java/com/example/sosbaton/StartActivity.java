package com.example.sosbaton;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class StartActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        db = FirebaseFirestore.getInstance();

        EditText etName = findViewById(R.id.etName);
        EditText etEmail = findViewById(R.id.etEmail);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "名前とメールを入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("name", name)
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (query.size() > 0) {
                            // ログイン成功 → 地図画面(MainActivity)
                            Intent intent = new Intent(StartActivity.this, MainActivity.class);
                            intent.putExtra("USER_NAME", name); // ← 名前を渡す！
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "一致するユーザーがいません", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "通信エラー：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }
}
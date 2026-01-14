package com.example.sosbaton;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button backButton = findViewById(R.id.button);
        backButton.setOnClickListener(v -> {
            // 単純に前の画面（MainActivity）へ戻る
            getOnBackPressedDispatcher().onBackPressed();
        });

    }
}
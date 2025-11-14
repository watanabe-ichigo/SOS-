package com.example.sosbaton;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Button backButton = findViewById(R.id.button);
        backButton.setOnClickListener(v -> {
            // 単純に前の画面（MainActivity）へ戻る
            getOnBackPressedDispatcher().onBackPressed();
        });
    }
}

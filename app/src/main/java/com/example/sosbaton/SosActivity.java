package com.example.sosbaton;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.Button;
import android.content.Intent;
import android.net.Uri;



public class SosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // activity_sos.xml を画面として使う
        setContentView(R.layout.activity_sos);
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // ← 戻る処理

        Button HelpButton = findViewById(R.id.HelpButton);
        HelpButton.setOnClickListener(v -> {
            // 電話アプリを起動して119を入力した状態にする
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:119"));
            startActivity(intent);
        });

        Button EmergencyButton = findViewById(R.id.EmergencyButton);
        EmergencyButton.setOnClickListener(v -> {
            // 電話アプリを起動して119を入力した状態にする
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:110"));
            startActivity(intent);
        });
    }


}

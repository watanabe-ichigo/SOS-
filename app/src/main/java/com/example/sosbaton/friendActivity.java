package com.example.sosbaton;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;


public class friendActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_friend);

        ImageButton close =findViewById(R.id.btnClose);

        close.setOnClickListener(v -> {
            finish();
        });
    }
}

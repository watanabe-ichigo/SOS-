package com.example.sosbaton;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class FriendmsgActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_massage);

        ImageButton close =findViewById(R.id.btnClose);

        close.setOnClickListener(v -> {
            finish();
        });
    }
}

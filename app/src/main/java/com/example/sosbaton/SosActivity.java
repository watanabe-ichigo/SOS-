package com.example.sosbaton;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class SosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // activity_sos.xml を画面として使う
        setContentView(R.layout.activity_sos);
    }
}

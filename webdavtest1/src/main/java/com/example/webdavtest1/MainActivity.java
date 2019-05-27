package com.example.webdavtest1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        A.mContext = this;
        setContentView(R.layout.activity_main);

    }

    public void b1Click(View view) {

    }
}

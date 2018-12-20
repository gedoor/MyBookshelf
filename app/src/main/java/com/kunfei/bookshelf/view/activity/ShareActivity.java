package com.kunfei.bookshelf.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();
        String type = getIntent().getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                SearchBookActivity.startByKey(this, getIntent().getStringExtra(Intent.EXTRA_TEXT));
            }
        }
    }
}

package com.kunfei.bookshelf.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ReceivingSharedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();
        String type = getIntent().getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String text= getIntent().getStringExtra(Intent.EXTRA_TEXT);
                if(openUrl(text))
                SearchBookActivity.startByKey(this,text);
                finish();
                return;
            }
        }
        if (Intent.ACTION_PROCESS_TEXT.equals(action) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && type != null) {
            if ("text/plain".equals(type)) {
                String text= getIntent().getStringExtra(Intent.EXTRA_PROCESS_TEXT);
                if(openUrl(text))
                    SearchBookActivity.startByKey(this,text);
                finish();
                return;
            }
        }
        finish();
    }

    private  boolean openUrl(String text){
        String[] urls=text.split("\\s");
        String result="";
        for(String url:urls){
            if(url.matches("http.+"))
                result=result+"\n"+url;
        }
        result=result.trim();
        if(result.length()>1){
            SharedPreferences.Editor editor = getSharedPreferences("CONFIG", MODE_MULTI_PROCESS).edit();
            editor.putString("shared_url", result);
            editor.commit();

            Intent intent = new Intent();
            //    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//                intent.putExtra("shared_url", url);
//                intent.putExtra("shared_time",System.currentTimeMillis());
            intent.setClass(ReceivingSharedActivity.this, MainActivity.class);
            this.startActivity(intent);
            return false;
        }

        return true;
    }


}

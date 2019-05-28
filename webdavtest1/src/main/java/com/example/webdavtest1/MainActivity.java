package com.example.webdavtest1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import org.webdav.WebDavFile;
import org.webdav.WebDavHelp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static org.webdav.WebDavHelp.initWebDav;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        A.mContext = this;
        setContentView(R.layout.activity_main);
        initWebDav();
    }

    public void b1Click(View view) {
        A.log("b1Click");
        new Thread(){
            @Override
            public void run() {
                try {
                    List<WebDavFile> webDavFiles = new WebDavFile(WebDavHelp.getWebDavUrl() + "YueDu/").listFiles();
                    A.log("size", webDavFiles.size());
                    for (WebDavFile file : webDavFiles) {
                        A.log("*"+file.getDisplayName(), file.getSize(), T.chinaTime(file.getLastModified()), file.isDirectory());
                    }
//                    webDavFiles.get(2).download("/sdcard/" + webDavFiles.get(2).getDisplayName(), true);
                } catch (Exception e) {
                    A.error(e);
                }
            }
        }.start();
    }

    public void b2Click(View view) {
        A.log("b2Click");
        new Thread(){
            @Override
            public void run() {
                try {
                    String localFile = "/sdcard/download/1.jpg";
                    new WebDavFile(WebDavHelp.getWebDavUrl() + "YueDuTest2").makeAsDir();
                    String putUrl = WebDavHelp.getWebDavUrl() + "YueDuTest2/" + T.getFilename(localFile);
                    WebDavFile webDavFile = new WebDavFile(putUrl);
                    webDavFile.upload(localFile);
                } catch (Exception e) {
                    A.error(e);
                }
            }
        }.start();

    }

    public void b3Click(View view) {
        A.log("b3Click");
        new Thread(){
            @Override
            public void run() {
                try {
                    List<WebDavFile> webDavFiles = new WebDavFile(WebDavHelp.getWebDavUrl() + "YueDu/").listFiles();
                    A.log("size(1)", webDavFiles.size());
                    for (WebDavFile file : webDavFiles) {
                        A.log("*"+file.getDisplayName(), file.getSize(), T.chinaTime(file.getLastModified()), file.isDirectory());
                    }
                    WebDavFile toDelete = webDavFiles.get(0);
                    A.log("delete ", toDelete.getDisplayName(), toDelete.delete());
                    webDavFiles = new WebDavFile(WebDavHelp.getWebDavUrl() + "YueDu/").listFiles();
                    A.log("size(2)", webDavFiles.size());
                    for (WebDavFile file : webDavFiles) {
                        A.log("*"+file.getDisplayName(), file.getSize(), T.chinaTime(file.getLastModified()), file.isDirectory());
                    }
                } catch (Exception e) {
                    A.error(e);
                }
            }
        }.start();
    }

    public void b4Click(View view) {
        A.log("b4Click");
        new Thread(){
            @Override
            public void run() {
                try {
                    List<WebDavFile> webDavFiles = new WebDavFile(WebDavHelp.getWebDavUrl() + "YueDuTest2/").listFiles();
                    A.log("size(1)", webDavFiles.size());
                    for (WebDavFile file : webDavFiles) {
                        A.log("*"+file.getDisplayName(), file.getSize(), T.chinaTime(file.getLastModified()), file.isDirectory());
                    }

                    WebDavFile file = webDavFiles.get(0);
                    String copyTo = WebDavHelp.getWebDavUrl() + "YueDu/" + T.getFilename(file.getUrl());
                    A.log("copy", copyTo, file.copy(copyTo));
                } catch (Exception e) {
                    A.error(e);
                }
            }
        }.start();
    }

    public void b5Click(View view) {
        A.log("b5Click");
        new Thread(){
            @Override
            public void run() {
                try {

                } catch (Exception e) {
                    A.error(e);
                }
            }
        }.start();
    }
}

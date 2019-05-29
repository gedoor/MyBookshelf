package com.example.webdavtest1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import org.webdav.WebDavFile;
import org.webdav.WebDavHelp;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

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
//                    List<WebDavFile> webDavFiles = new WebDavFile(WebDavHelp.getWebDavUrl() + "YueDu/").listFiles();
                    List<WebDavFile> webDavFiles = new WebDavFile(WebDavHelp.getWebDavUrl() + "/").listFiles();
                    A.log("size", webDavFiles.size());
                    for (WebDavFile file : webDavFiles) {
                        A.log("*"+file.getDisplayName(), file.getSize(), T.chinaTime(file.getLastModified()), file.isDirectory());
                    }

/*                    WebDavFile toDownload = webDavFiles.get(0);
//                    toDownload.download("/sdcard/" + toDownload.getDisplayName(), true);
                    ByteArrayOutputStream os = toDownload.download();
                    OutputStream out = new FileOutputStream("/sdcard/" + toDownload.getDisplayName());
                    os.writeTo(out);*/
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
                    new WebDavFile(WebDavHelp.getWebDavUrl() + "/YueDuTest2").markDirs();
                    String putUrl = WebDavHelp.getWebDavUrl() + "/YueDuTest2/x" + T.getFilename(localFile);
                    WebDavFile webDavFile = new WebDavFile(putUrl);
//                    webDavFile.upload(localFile);
                    webDavFile.upload(T.file2InputStream(localFile));
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
                    List<WebDavFile> webDavFiles = new WebDavFile(WebDavHelp.getWebDavUrl() + "/YueDu/").listFiles();
                    A.log("size(1)", webDavFiles.size());
                    for (WebDavFile file : webDavFiles) {
                        A.log("*"+file.getDisplayName(), file.getSize(), T.chinaTime(file.getLastModified()), file.isDirectory());
                    }
                    WebDavFile toDelete = webDavFiles.get(0);
                    A.log("delete ", toDelete.getDisplayName(), toDelete.delete());
                    webDavFiles = new WebDavFile(WebDavHelp.getWebDavUrl() + "/YueDu/").listFiles();
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
                    List<WebDavFile> webDavFiles = new WebDavFile(WebDavHelp.getWebDavUrl() + "/YueDuTest2/").listFiles();
                    A.log("size(1)", webDavFiles.size());
                    for (WebDavFile file : webDavFiles) {
                        A.log("*"+file.getDisplayName(), file.getSize(), T.chinaTime(file.getLastModified()), file.isDirectory());
                    }

                    WebDavFile file = webDavFiles.get(0);
                    String copyTo = WebDavHelp.getWebDavUrl() + "/test21/" + T.getFilename(file.getUrl());
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
                    /*WebDavFile webFile = new WebDavFile(WebDavHelp.getWebDavUrl() + "YueDuTest2/cc/1.jpg");
                    String moveTo = WebDavHelp.getWebDavUrl() + "YueDuTest2/cc/2.jpg";
                    A.log("move", moveTo, webFile.move(moveTo), webFile.code);*/

                    A.log(1, new WebDavFile(WebDavHelp.getWebDavUrl() ).exists());
                    A.log(1, new WebDavFile(WebDavHelp.getWebDavUrl() + "/YueDuTest2").exists());
                    A.log(2, new WebDavFile(WebDavHelp.getWebDavUrl() + "/YueDuTest2/1.jpg").exists());
                    A.log(3, new WebDavFile(WebDavHelp.getWebDavUrl() + "/YueDuTest2/111.jpg").exists());

/*                    long t = System.currentTimeMillis();
                    boolean b = new WebDavFile(WebDavHelp.getWebDavUrl() + "/YueDuTest4/asub1/asub2").markDirs();
                    A.log(4, b, System.currentTimeMillis() - t);*/

                } catch (Exception e) {
                    A.error(e);
                }
            }
        }.start();
    }
}

//Copyright (c) 2017. 章钦豪. All rights reserved.
package com.monke.monkeybook;

import com.monke.monkeybook.base.observer.SimpleObserver;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ErrorAnalyContentManager {
    private ErrorAnalyContentManager(){

    }
    private static ErrorAnalyContentManager instance;

    public static ErrorAnalyContentManager getInstance(){
        if(instance == null){
            synchronized (ErrorAnalyContentManager.class){
                if(instance == null){
                    instance = new ErrorAnalyContentManager();
                }
            }
        }
        return instance;
    }

    public void writeNewErrorUrl(final String url){
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            String filePath = MApplication.getInstance().getExternalFilesDir("").getPath();
            File dir = new File(filePath);
            if(!dir.exists()){
                dir.mkdirs();
            }

            File file2 = new File(filePath,"ErrorAnalyUrlsDetail.txt");
            if(!file2.exists()) {
                file2.createNewFile();
            }
            FileOutputStream fileOutputStream2 = new FileOutputStream(file2,true);
            fileOutputStream2.write((url+"    \r\n").getBytes());
            fileOutputStream2.flush();
            fileOutputStream2.close();
            ///////////////////////////////////////////////////////////////////////
            File file1 = new File(filePath,"ErrorAnalyUrls.txt");
            if(!file1.exists()) {
                file1.createNewFile();
            }
            FileInputStream inputStream = new FileInputStream(file1);
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            while (inputStream.read(bytes) != -1) {
                arrayOutputStream.write(bytes, 0, bytes.length);
            }
            inputStream.close();
            arrayOutputStream.close();
            String content = new String(arrayOutputStream.toByteArray());
            if(!content.contains(url.substring(0,url.indexOf('/',8)))){
                FileOutputStream fileOutputStream1 = new FileOutputStream(file1,true);
                fileOutputStream1.write((url.substring(0,url.indexOf('/',8))+"    \r\n").getBytes());
                fileOutputStream1.flush();
                fileOutputStream1.close();
            }
            e.onNext(true);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public void writeMayByNetError(final String url){
        Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            String filePath = MApplication.getInstance().getExternalFilesDir("").getPath();
            File dir = new File(filePath);
            if(!dir.exists()){
                dir.mkdirs();
            }

            File file = new File(filePath,"ErrorNetUrl.txt");
            if(!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream2 = new FileOutputStream(file,true);
            fileOutputStream2.write((url+"    \r\n").getBytes());
            fileOutputStream2.flush();
            fileOutputStream2.close();
            e.onNext(true);
            e.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean value) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }
}

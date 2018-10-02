package com.monke.monkeybook.help;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.monke.basemvplib.BaseModelImpl;
import com.monke.monkeybook.MApplication;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.observer.SimpleObserver;
import com.monke.monkeybook.model.analyzeRule.AnalyzeHeaders;
import com.monke.monkeybook.model.impl.IHttpGetApi;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class UpdateManager extends BaseModelImpl {

    public void checkUpdate(final Context context) {
        getRetrofitString("https://api.github.com")
                .create(IHttpGetApi.class)
                .getWebContent(MApplication.getInstance().getString(R.string.latest_release_api), AnalyzeHeaders.getMap(null))
                .flatMap(response -> analyzeLastReleaseApi(response.body()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<UpdateInfo>() {
                    @Override
                    public void onNext(UpdateInfo updateInfo) {
                        Toast.makeText(context, "有新版本" + updateInfo.lastVersion, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context, "检测新版本出错", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Observable<UpdateInfo> analyzeLastReleaseApi(String jsonStr) {
        return Observable.create(emitter -> {
            try {
                JsonObject version = new JsonParser().parse(jsonStr).getAsJsonObject();
                boolean prerelease = version.get("prerelease").getAsBoolean();
                if (prerelease)
                    return;
                JsonArray assets = version.get("assets").getAsJsonArray();
                if (assets.size() > 0) {
                    String lastVersion = version.get("tag_name").getAsString().substring(1);
                    String url = assets.get(0).getAsJsonObject().get("browser_download_url").getAsString();
                    String detail = version.get("body").getAsString();
                    if (Integer.valueOf(lastVersion.split(".")[2]) > Integer.valueOf(MApplication.getVersionName().split(".")[2])) {
                        UpdateInfo updateInfo = new UpdateInfo();
                        updateInfo.url = url;
                        updateInfo.lastVersion = lastVersion;
                        updateInfo.detail = detail;
                        emitter.onNext(updateInfo);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                emitter.onError(new Throwable("检测更新失败!"));
            }
            emitter.onComplete();
        });
    }

    /**
     * 安装apk
     */
    private void installApk(Context context, File apkfile) {
        if (!apkfile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Logger.d("UpdateManager", apkfile.toString());
        intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    private class UpdateInfo {
        String lastVersion;
        String url;
        String detail;
    }
}

package com.kunfei.bookshelf.base;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.bean.CookieBean;
import com.kunfei.bookshelf.help.EncodeConverter;
import com.kunfei.bookshelf.help.SSLSocketClient;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;
import com.kunfei.bookshelf.model.impl.IHttpGetApi;
import com.kunfei.bookshelf.model.impl.IHttpPostApi;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class BaseModelImpl {
    private static OkHttpClient httpClient;

    public static BaseModelImpl getInstance() {
        return new BaseModelImpl();
    }

    public Observable<Response<String>> getResponseO(AnalyzeUrl analyzeUrl) {
        switch (analyzeUrl.getUrlMode()) {
            case POST:
                return getRetrofitString(analyzeUrl.getHost(), analyzeUrl.getCharCode())
                        .create(IHttpPostApi.class)
                        .postMap(analyzeUrl.getPath(),
                                analyzeUrl.getQueryMap(),
                                analyzeUrl.getHeaderMap());
            case GET:
                return getRetrofitString(analyzeUrl.getHost(), analyzeUrl.getCharCode())
                        .create(IHttpGetApi.class)
                        .getMap(analyzeUrl.getPath(),
                                analyzeUrl.getQueryMap(),
                                analyzeUrl.getHeaderMap());
            default:
                return getRetrofitString(analyzeUrl.getHost(), analyzeUrl.getCharCode())
                        .create(IHttpGetApi.class)
                        .get(analyzeUrl.getPath(),
                                analyzeUrl.getHeaderMap());
        }
    }

    public Retrofit getRetrofitString(String url) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodeConverter.create())
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getClient())
                .build();
    }

    public Retrofit getRetrofitString(String url, String encode) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodeConverter.create(encode))
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getClient())
                .build();
    }

    synchronized public static OkHttpClient getClient() {
        if (httpClient == null) {
            ArrayList<ConnectionSpec> specs = new ArrayList<>();
            specs.add(ConnectionSpec.MODERN_TLS);
            specs.add(ConnectionSpec.COMPATIBLE_TLS);
            specs.add(ConnectionSpec.CLEARTEXT);
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.createTrustAllManager())
                    .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .connectionSpecs(specs)
                    .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .addInterceptor(getHeaderInterceptor())
                    .build();
        }
        return httpClient;
    }

    private static Interceptor getHeaderInterceptor() {
        return chain -> {
            Request request = chain.request()
                    .newBuilder()
                    .addHeader("Keep-Alive", "300")
                    .addHeader("Connection", "Keep-Alive")
                    .addHeader("Cache-Control", "no-cache")
                    .build();
            return chain.proceed(request);
        };
    }

    protected Observable<Response<String>> setCookie(Response<String> response, String tag) {
        return Observable.create(e -> {
            if (!response.raw().headers("Set-Cookie").isEmpty()) {
                StringBuilder cookieBuilder = new StringBuilder();
                for (String s : response.raw().headers("Set-Cookie")) {
                    String[] x = s.split(";");
                    for (String y : x) {
                        if (!TextUtils.isEmpty(y)) {
                            cookieBuilder.append(y).append(";");
                        }
                    }
                }
                String cookie = cookieBuilder.toString();
                if (!TextUtils.isEmpty(cookie)) {
                    DbHelper.getDaoSession().getCookieBeanDao().insertOrReplace(new CookieBean(tag, cookie));
                }
            }
            e.onNext(response);
            e.onComplete();
        });
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    protected Observable<String> getAjaxString(AnalyzeUrl analyzeUrl, String tag, String js) {
        final Web web = new Web("加载超时");
        if (!TextUtils.isEmpty(js)) {
            web.js = js;
        }
        return Observable.create(e -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                Runnable timeoutRunnable;
                WebView webView = new WebView(MApplication.getInstance());
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setUserAgentString(analyzeUrl.getHeaderMap().get("User-Agent"));
                CookieManager cookieManager = CookieManager.getInstance();
                Runnable retryRunnable = new Runnable() {
                    @Override
                    public void run() {
                        webView.evaluateJavascript(web.js, value -> {
                            if (!TextUtils.isEmpty(value)) {
                                web.content = StringEscapeUtils.unescapeJson(value);
                                e.onNext(web.content);
                                e.onComplete();
                                webView.destroy();
                                handler.removeCallbacks(this);
                            } else {
                                handler.postDelayed(this, 1000);
                            }
                        });
                    }
                };
                timeoutRunnable = () -> {
                    if (!e.isDisposed()) {
                        handler.removeCallbacks(retryRunnable);
                        e.onNext(web.content);
                        e.onComplete();
                        webView.destroy();
                    }
                };
                handler.postDelayed(timeoutRunnable, 30000);
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        DbHelper.getDaoSession().getCookieBeanDao()
                                .insertOrReplace(new CookieBean(tag, cookieManager.getCookie(webView.getUrl())));
                        handler.postDelayed(retryRunnable, 1000);
                    }
                });
                switch (analyzeUrl.getUrlMode()) {
                    case POST:
                        webView.postUrl(analyzeUrl.getUrl(), analyzeUrl.getPostData());
                        break;
                    case GET:
                        webView.loadUrl(String.format("%s?%s", analyzeUrl.getUrl(), analyzeUrl.getQueryStr()), analyzeUrl.getHeaderMap());
                        break;
                    default:
                        webView.loadUrl(analyzeUrl.getUrl(), analyzeUrl.getHeaderMap());
                }
            });
        });
    }

    private static class Web {
        private String content;
        private String js = "document.documentElement.outerHTML";

        Web(String content) {
            this.content = content;
        }
    }

}
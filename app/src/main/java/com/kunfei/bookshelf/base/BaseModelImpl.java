package com.kunfei.bookshelf.base;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.bean.CookieBean;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.help.EncodeConverter;
import com.kunfei.bookshelf.help.HttpInterceptor;
import com.kunfei.bookshelf.help.SSLSocketClient;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;
import com.kunfei.bookshelf.model.impl.IHttpGetApi;
import com.kunfei.bookshelf.model.impl.IHttpPostApi;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

public class BaseModelImpl {
    private static OkHttpClient.Builder clientBuilder;

    public static BaseModelImpl getInstance() {
        return new BaseModelImpl();
    }

    public Observable<Response<String>> getResponseO(AnalyzeUrl analyzeUrl) {
        switch (analyzeUrl.getUrlMode()) {
            case POST:
                return getRetrofitString(analyzeUrl.getHost())
                        .create(IHttpPostApi.class)
                        .searchBook(analyzeUrl.getPath(),
                                analyzeUrl.getQueryMap(),
                                analyzeUrl.getHeaderMap());
            case GET:
                return getRetrofitString(analyzeUrl.getHost())
                        .create(IHttpGetApi.class)
                        .searchBook(analyzeUrl.getPath(),
                                analyzeUrl.getQueryMap(),
                                analyzeUrl.getHeaderMap());
            default:
                return getRetrofitString(analyzeUrl.getHost())
                        .create(IHttpGetApi.class)
                        .getWebContent(analyzeUrl.getPath(),
                                analyzeUrl.getHeaderMap());
        }
    }

    public Retrofit getRetrofitString(String url) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodeConverter.create())
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getClientBuilder().build())
                .build();
    }

    public Retrofit getRetrofitString(String url, String encode) {
        return new Retrofit.Builder().baseUrl(url)
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(EncodeConverter.create(encode))
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(getClientBuilder().build())
                .build();
    }

    private static OkHttpClient.Builder getClientBuilder() {
        if (clientBuilder == null) {
            clientBuilder = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.createTrustAllManager())
                    .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                    .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .addInterceptor(getHeaderInterceptor())
                    .addInterceptor(new HttpInterceptor(1));
        }
        return clientBuilder;
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
    protected Observable<String> getAjaxHtml(AnalyzeUrl analyzeUrl, String sourceUrl) {
        return Observable.create(e -> {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                class HtmlOutJavaScriptInterface {
                    private WebView webView;

                    private HtmlOutJavaScriptInterface(WebView webView) {
                        this.webView = webView;
                    }

                    @SuppressWarnings("unused")
                    @JavascriptInterface
                    public void processHTML(String html) {
                        e.onNext(html);
                        e.onComplete();
                        webView.destroy();
                    }
                }
                WebView webView = new WebView(MApplication.getInstance());
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setUserAgentString(analyzeUrl.getHeaderMap().get("User-Agent"));
                webView.addJavascriptInterface(new HtmlOutJavaScriptInterface(webView), "HTML_OUT");
                CookieManager cookieManager = CookieManager.getInstance();
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        DbHelper.getDaoSession().getCookieBeanDao().insertOrReplace(new CookieBean(sourceUrl, cookieManager.getCookie(webView.getUrl())));
                        webView.loadUrl("javascript:window.HTML_OUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                    }
                });
                switch (analyzeUrl.getUrlMode()) {
                    case POST:
                        webView.postUrl(analyzeUrl.getUrl(), analyzeUrl.getQueryStr().getBytes());
                        break;
                    case GET:
                        webView.loadUrl(String.format("%s?%s", analyzeUrl.getUrl(), analyzeUrl.getQueryStr()));
                        break;
                    default:
                        webView.loadUrl(analyzeUrl.getUrl());
                }
            });
        });
    }

}
package com.monke.basemvplib;

import android.support.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import static android.text.TextUtils.isEmpty;

public class EncodeConverter extends Converter.Factory {

    private EncodeConverter(){

    }

    public static EncodeConverter create(){
        return new EncodeConverter();
    }

    @Override
    public Converter<ResponseBody, String> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new Converter<ResponseBody, String>() {
            @Override
            public String convert(@NonNull ResponseBody value) throws IOException {
                String charsetStr = null;
                MediaType mediaType = value.contentType();
                if (mediaType != null) {
                    Charset charset = mediaType.charset();
                    if (charset != null) {
                        charsetStr = charset.toString();
                    }
                }
                byte[] responseBytes = value.bytes();
                if (!isEmpty(charsetStr)) {
                    return new String(responseBytes, charsetStr);
                }
                byte[] headerBytes = Arrays.copyOfRange(responseBytes, 0, 1024);
                Document doc = Jsoup.parse(new String(headerBytes, "utf-8"));
                Elements metaTags = doc.getElementsByTag("meta");
                for (Element metaTag : metaTags) {
                    String content = metaTag.attr("content");
                    String http_equiv = metaTag.attr("http-equiv");
                    charsetStr = metaTag.attr("charset");
                    if (!charsetStr.isEmpty()) {
                        break;
                    }
                    if (http_equiv.toLowerCase().equals("content-type")) {
                        charsetStr = content.substring(content.toLowerCase().indexOf("charset") + "charset=".length());
                        break;
                    }
                }
                if (!isEmpty(charsetStr)) {
                    return new String(responseBytes, charsetStr);
                }
                UniversalDetector detector = new UniversalDetector(null);
                detector.handleData(responseBytes, 0, 2000);
                detector.dataEnd();
                return new String(responseBytes, detector.getDetectedCharset());
            }
        };
    }
}

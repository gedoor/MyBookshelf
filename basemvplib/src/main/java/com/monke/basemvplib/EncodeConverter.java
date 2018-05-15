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
                String charsetStr;
                MediaType mediaType = value.contentType();
                byte[] responseBytes = value.bytes();
                //根据http头判断
                if (mediaType != null) {
                    Charset charset = mediaType.charset();
                    if (charset != null) {
                        charsetStr = charset.displayName();
                        if (!isEmpty(charsetStr)) {
                            return new String(responseBytes, Charset.forName(charsetStr));
                        }
                    }
                }
                //根据meta判断
                byte[] headerBytes = Arrays.copyOfRange(responseBytes, 0, 1024);
                Document doc = Jsoup.parse(new String(headerBytes, "utf-8"));
                Elements metaTags = doc.getElementsByTag("meta");
                for (Element metaTag : metaTags) {
                    String content = metaTag.attr("content");
                    String http_equiv = metaTag.attr("http-equiv");
                    charsetStr = metaTag.attr("charset");
                    if (!charsetStr.isEmpty()) {
                        if (!isEmpty(charsetStr)) {
                            return new String(responseBytes, Charset.forName(charsetStr));
                        }
                    }
                    if (http_equiv.toLowerCase().equals("content-type")) {
                        if (content.toLowerCase().contains("charset")) {
                            charsetStr = content.substring(content.toLowerCase().indexOf("charset") + "charset=".length());
                        } else {
                            charsetStr = content.substring(content.toLowerCase().indexOf(";") + 1);
                        }
                        if (!isEmpty(charsetStr)) {
                            try {
                                return new String(responseBytes, Charset.forName(charsetStr));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                //根据内容判断
                UniversalDetector detector = new UniversalDetector(null);
                detector.handleData(responseBytes, 0, 2000);
                detector.dataEnd();
                charsetStr = detector.getDetectedCharset();
                return new String(responseBytes, Charset.forName(charsetStr));
            }
        };
    }
}

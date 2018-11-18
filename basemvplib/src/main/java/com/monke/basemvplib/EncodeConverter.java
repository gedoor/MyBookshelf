package com.monke.basemvplib;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
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
    private String encode;

    private EncodeConverter(){

    }

    private EncodeConverter(String encode){
        this.encode = encode;
    }

    public static EncodeConverter create(){
        return new EncodeConverter();
    }

    public static EncodeConverter create(String en){
        return new EncodeConverter(en);
    }

    @Override
    public Converter<ResponseBody, String> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return value -> {
            if (!TextUtils.isEmpty(encode)) {
                return new String((value.bytes()), encode);
            }
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
            charsetStr = CharsetDetector.detectCharset(new ByteArrayInputStream(responseBytes));
            return new String(responseBytes, Charset.forName(charsetStr));
        };
    }
}

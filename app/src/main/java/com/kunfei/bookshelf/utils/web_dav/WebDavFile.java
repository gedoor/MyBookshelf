package com.kunfei.bookshelf.utils.web_dav;

import com.kunfei.bookshelf.utils.web_dav.http.Handler;
import com.kunfei.bookshelf.utils.web_dav.http.HttpAuth;
import com.kunfei.bookshelf.utils.web_dav.http.OkHttp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebDavFile {
    public final static String TAG = WebDavFile.class.getSimpleName();
    public final static String OBJECT_NOT_EXISTS_TAG = "ObjectNotFound";
    // 指定返回哪些属性
    private final static String DIR = "<?xml version=\"1.0\"?>\n" +
            "<a:propfind xmlns:a=\"DAV:\">\n" +
            "<a:prop>\n" +
            "<a:displayname/>\n<a:resourcetype/>\n<a:getcontentlength/>\n<a:creationdate/>\n<a:getlastmodified/>\n%s" +
            "</a:prop>\n" +
            "</a:propfind>";

    private URL url;
    private String httpUrl;

    private String displayName;
    private long createTime;
    private long lastModified;
    private long size;
    private boolean isDirectory = true;
    private boolean exists = false;
    private String parent = "";
    private String urlName = "";

    private OkHttpClient okHttpClient;

    public WebDavFile(String url) throws MalformedURLException {
        this.url = new URL(null, url, Handler.HANDLER);
        okHttpClient = OkHttp.getInstance().client();
    }

    public String getUrl() {
        if (httpUrl == null) {
            String raw = url.toString().replace("davs://", "https://").replace("dav://", "http://");
            try {
                httpUrl = URLEncoder.encode(raw, "UTF-8")
                        .replaceAll("\\+", "%20")
                        .replaceAll("%3A", ":")
                        .replaceAll("%2F", "/");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return httpUrl;
    }

    public String getPath() {
        return url.toString();
    }

    /**
     * 填充文件信息。实例化WebDAVFile对象时，并没有将远程文件的信息填充到实例中。需要手动填充！
     *
     * @return 远程文件是否存在
     */
    public boolean indexFileInfo() throws IOException {
        Response response = propFindResponse(new ArrayList<>());
        String s = "";
        try {
            if (response == null || !response.isSuccessful()) {
                this.exists = false;
                return false;
            }
            s = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }

    /**
     * 列出当前路径下的文件。默认列出文件的如下属性：displayname、resourcetype、getcontentlength、creationdate、getlastmodified
     *
     * @return 文件列表
     */
    public List<WebDavFile> listFiles() throws IOException {
        return listFiles(new ArrayList<>());
    }

    /**
     * 列出当前路径下的文件
     *
     * @param propsList 指定列出文件的哪些属性
     * @return 文件列表
     */
    public List<WebDavFile> listFiles(ArrayList<String> propsList) throws IOException {
        Response response = propFindResponse(propsList);
        try {
            assert response != null;
            if (response.isSuccessful()) {
                return parseDir(response.body().string());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private Response propFindResponse(ArrayList<String> propsList) throws IOException {
        return propFindResponse(propsList, 1);
    }

    private Response propFindResponse(ArrayList<String> propsList, int depth) throws IOException {
        StringBuilder requestProps = new StringBuilder();
        for (String p : propsList) {
            requestProps.append("<a:").append(p).append("/>\n");
        }
        String requestPropsStr;
        if (requestProps.toString().isEmpty()) {
            requestPropsStr = DIR.replace("%s", "");
        } else {
            requestPropsStr = String.format(DIR, requestProps.toString() + "\n");
        }
        Request.Builder request = new Request.Builder()
                .url(getUrl())
                // 添加RequestBody对象，可以只返回的属性。如果设为null，则会返回全部属性
                // 注意：尽量手动指定需要返回的属性。若返回全部属性，可能后由于Prop.java里没有该属性名，而崩溃。
                .method("PROPFIND", RequestBody.create(MediaType.parse("text/plain"), requestPropsStr));

        HttpAuth.Auth auth = HttpAuth.getAuth();
        if (auth != null) {
            request.header("Authorization", Credentials.basic(auth.getUser(), auth.getPass()));
        }
        request.header("Depth", depth < 0 ? "infinity" : Integer.toString(depth));

        return okHttpClient.newCall(request.build()).execute();
    }

    private List<WebDavFile> parseDir(String s) {
        List<WebDavFile> list = new ArrayList<>();
        Document document = Jsoup.parse(s);
        Elements elements = document.getElementsByTag("d:response");
        String baseUrl = getUrl().endsWith("/") ? getUrl() : getUrl() + "/";
        for (Element element : elements) {
            String href = element.getElementsByTag("d:href").get(0).text();
            if (!href.endsWith("/")) {
                String fileName = href.substring(href.lastIndexOf("/") + 1);
                WebDavFile webDavFile;
                try {
                    webDavFile = new WebDavFile(baseUrl + fileName);
                    webDavFile.setDisplayName(fileName);
                    webDavFile.setUrlName(href);
                    list.add(webDavFile);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public InputStream getInputStream() {
        Request.Builder request = new Request.Builder()
                .url(getUrl());

        HttpAuth.Auth auth = HttpAuth.getAuth();

        if (auth != null) {
            request.header("Authorization", Credentials.basic(auth.getUser(), auth.getPass()));
        }

        try {
            Response response = okHttpClient.newCall(request.build()).execute();
            return response.body().byteStream();
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据自己的URL，在远程处创建对应的文件夹
     *
     * @return 是否创建成功
     */
    public boolean makeAsDir() throws IOException {
        Request.Builder request = new Request.Builder()
                .url(getUrl())
                .method("MKCOL", null);
        return execRequest(request);
    }

    /**
     * 下载到本地
     *
     * @param savedPath       本地的完整路径，包括最后的文件名
     * @param replaceExisting 是否替换本地的同名文件
     * @return 下载是否成功
     */
    public boolean download(String savedPath, boolean replaceExisting) {
        File file = new File(savedPath);
        if (file.exists()) {
            if (replaceExisting) {
                file.delete();
            } else {
                return false;
            }
        }
        InputStream in = getInputStream();
        FileOutputStream out = null;
        try {
            file.createNewFile();
            out = new FileOutputStream(file);
            byte[] buffer = new byte[1024 * 8];
            int byteRead;
            while ((byteRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteRead);
            }
            out.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 上传文件
     *
     * @param localPath 本地文件路径
     * @return 是否成功成功
     */
    public boolean upload(String localPath) throws IOException {
        return upload(localPath, null);
    }

    public boolean upload(String localPath, String contentType) throws IOException {
        File file = new File((localPath));
        if (!file.exists()) return false;
        MediaType mediaType = contentType == null ? null : MediaType.parse(contentType);
        // 务必注意RequestBody不要嵌套，不然上传时内容可能会被追加多余的文件信息
        RequestBody fileBody = RequestBody.create(mediaType, file);
        Request.Builder request = new Request.Builder()
                .url(getUrl())
                .put(fileBody);

        return execRequest(request);
    }

    /**
     * 执行请求，获取响应结果
     *
     * @param requestBuilder 因为还需要追加验证信息，所以此处传递Request.Builder的对象，而不是Request的对象
     * @return 请求执行的结果
     */
    private boolean execRequest(Request.Builder requestBuilder) throws IOException {
        HttpAuth.Auth auth = HttpAuth.getAuth();
        if (auth != null) {
            requestBuilder.header("Authorization", Credentials.basic(auth.getUser(), auth.getPass()));
        }

        Response response = okHttpClient.newCall(requestBuilder.build()).execute();
        return response.isSuccessful();
    }

    /**
     * 打印对象内的所有属性
     */
    public static <T> T printAllAttrs(String className, Object o) {
        try {
            Class<?> c = Class.forName(className);
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
            }
            System.out.println("=============" + className + "===============");
            for (Field f : fields) {
                String field = f.toString().substring(f.toString().lastIndexOf(".") + 1); //取出属性名称
                System.out.println(field + " --> " + f.get(o));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setUrlName(String urlName) {
        this.urlName = urlName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getURLName() {
        if (urlName.isEmpty()) {
            urlName = (parent.isEmpty() ? url.getFile() : url.toString().replace(parent, "")).
                    replace("/", "");
        }
        return urlName;
    }

    public String getHost() {
        return url.getHost();
    }

    public boolean canRead() {
        return true;
    }

    public boolean canWrite() {
        return false;
    }

    public boolean exists() {
        return exists;
    }

    public void exists(boolean exists) {
        this.exists = exists;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String path) {
        parent = path;
    }

    public void setIsDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }
}
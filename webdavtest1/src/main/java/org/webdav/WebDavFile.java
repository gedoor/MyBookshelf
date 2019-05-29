package org.webdav;

import android.net.Uri;

import com.example.webdavtest1.A;
import com.example.webdavtest1.T;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.webdav.http.WHandler;
import org.webdav.http.WAuth;
import org.webdav.http.WHttp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private String url_Str;
    private String httpUrl;

    private String displayName;
    private long createTime;
    private long lastModified;
    private long size;
    private boolean isDirectory = true;
    private String parent = "";
    private String urlName = "";
    public int code;
    public String responseText;

    private OkHttpClient okHttpClient;

    public WebDavFile(String url) throws MalformedURLException {
        url_Str = url;
        this.url = new URL(null, url, WHandler.HANDLER);
        okHttpClient = WHttp.getInstance().client();
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
                A.error(e);
            }
        }
        return httpUrl;
    }

    public String getPath() {
        return url.toString();
    }

    private Boolean exists;
    public boolean exists(){
        if (exists == null)
        try {
            Response response = propFindResponse(new ArrayList<String>());
            exists = response != null && response.isSuccessful();
            if (exists){
                String body = response.body().string();
                setIsDirectory(body.contains("d:collection"));
                if (!isDirectory) {
                    List<WebDavFile> wdFiles = parseDir(body);
                    if (wdFiles.size() == 1) {
                        setLastModified(wdFiles.get(0).getLastModified());
                        setSize(wdFiles.get(0).getSize());
                        setDisplayName(wdFiles.get(0).getDisplayName());
                    }
                }
            }
            A.log(T.getFilename(getUrl()), "exits", exists, "isFolder", isDirectory, "size", getSize());
        } catch (Exception e) {
            A.error(e);
            return false;
        }
        return exists;
    }

    /**
     * 列出当前路径下的文件。默认列出文件的如下属性：displayname、resourcetype、getcontentlength、creationdate、getlastmodified
     *
     * @return 文件列表
     */
    public List<WebDavFile> listFiles() throws IOException {
        return listFiles(new ArrayList<String>());
    }

    /**
     * 列出当前路径下的文件
     *
     * @param propsList 指定列出文件的哪些属性
     * @return 文件列表
     */
    public List<WebDavFile> listFiles(ArrayList<String> propsList) throws IOException {
        try {
            Response response = propFindResponse(propsList);
            code = response.code();
            A.log("responese code: " + code, response.toString());
            responseText = response.toString();
            if (response.isSuccessful()) {
                return parseDir(response.body().string());
            }//else A.log("failed", response.body().string());
        } catch (Exception e) {
            A.error(e);
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

        WAuth.Auth auth = WAuth.getAuth();
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
        String domainUrl = baseUrl.substring(0, baseUrl.indexOf("/", 10));
        for (Element element : elements) {
            String href = element.getElementsByTag("d:href").get(0).text();
            if (!baseUrl.endsWith(href))
            try {
                String fileName = href.substring(href.lastIndexOf("/") + 1);
                if (fileName.length() > 0)
                    fileName = baseUrl + fileName;
                else if (href.startsWith("/"))
                    fileName = domainUrl + href;

                String display = element.getElementsByTag("d:displayname").get(0).text();
                String lastModified = element.getElementsByTag("d:getlastmodified").get(0).text();
                String size = element.getElementsByTag("d:getcontentlength").get(0).text();
                String type = element.getElementsByTag("d:resourcetype").toString();

                if (T.isNull(display)){
                    String tmp = fileName.endsWith("/")? T.getFilePath(fileName) : fileName;
                    display = Uri.decode(T.getFilename(tmp));
                }

                WebDavFile webDavFile;
                webDavFile = new WebDavFile(fileName);
                webDavFile.setDisplayName(display);
                webDavFile.setUrlName(href);
                webDavFile.setSize(T.string2Int(size));
//                webDavFile.setIsDirectory(type.equals("httpd/unix-directory"));
                webDavFile.setIsDirectory(type.contains("d:collection"));
                webDavFile.setLastModified(lastModifiedToLong(lastModified));
                list.add(webDavFile);
            } catch (Exception e) {
                A.error(e);
            }
        }
        return list;
    }

    private long lastModifiedToLong(String input) {
        //坚果云 Mon, 27 May 2019 09:00:38 GMT
        //box   Wed, 29 May 2019 05:17:26 GMT
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat
                    ("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            Date date = inputFormat.parse(input);
            return date.getTime();
        } catch (Exception e) {
            A.error(e);
            return 0;
        }
    }

    public InputStream getInputStream() {
        Request.Builder request = new Request.Builder()
                .url(getUrl());

        WAuth.Auth auth = WAuth.getAuth();

        if (auth != null) {
            request.header("Authorization", Credentials.basic(auth.getUser(), auth.getPass()));
        }

        try {
            Response response = okHttpClient.newCall(request.build()).execute();
            code = response.code();
            return response.body().byteStream();
        } catch (IOException | IllegalArgumentException e) {
            A.error(e);
        }
        return null;
    }

    /**
     * 根据自己的URL，在远程处创建对应的文件夹
     *
     * @return 是否创建成功
     */
    public boolean markDirs() throws IOException {
        if (exists())
            return true;

        WebDavFile parent = new WebDavFile(T.getFilePath(url_Str));
        if (!parent.exists())
            if (!parent.markDirs())
                return false;

        Request.Builder request = new Request.Builder()
                .url(getUrl())
                .method("MKCOL", null);
        return execRequest(request);
    }

    public boolean delete() throws IOException {
        Request.Builder request = new Request.Builder()
                .url(getUrl())
                .method("DELETE", null);
        return execRequest(request);
    }

    public boolean copy(String toFile) throws IOException {
        Request.Builder request = new Request.Builder()
                .url(getUrl())
                .method("COPY", null);
        request.header("destination", toFile);
        return execRequest(request);
    }

    public boolean move(String toFile) throws IOException {
        Request.Builder request = new Request.Builder()
                .url(getUrl())
                .method("MOVE", null);
        request.header("destination", toFile);
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
            long downloaded = 0;
            out = new FileOutputStream(file);
            byte[] buffer = new byte[1024 * 8];
            int byteRead;
            while ((byteRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteRead);
                downloaded += byteRead;
                A.log(downloaded, getSize());
            }
            out.flush();
            return true;
        } catch (Exception e) {
            A.error(e);
        } finally {
            try {
                if (out != null) out.close();
            } catch (Exception e) {
                A.error(e);
            }
        }
        return false;
    }

    public ByteArrayOutputStream download() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = getInputStream();
        try {
            long downloaded = 0;
            byte[] buffer = new byte[1024 * 8];
            int byteRead;
            while ((byteRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteRead);
                downloaded += byteRead;
                A.log(downloaded, getSize());
            }
            return out;
        } catch (Exception e) {
            A.error(e);
        } finally {
            try {
                if (out != null) out.close();
            } catch (Exception e) {
                A.error(e);
            }
        }
        return null;
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

    public boolean upload(InputStream is) throws IOException {
        RequestBody fileBody = RequestBody.create(null, T.InputStream2Byte(is));
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
        WAuth.Auth auth = WAuth.getAuth();
        if (auth != null) {
            requestBuilder.header("Authorization", Credentials.basic(auth.getUser(), auth.getPass()));
        }

        Response response = okHttpClient.newCall(requestBuilder.build()).execute();
        code = response.code();
        A.log("execResponse", code, response);
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
            A.error(e);
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
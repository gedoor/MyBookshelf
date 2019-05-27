package org.webdav;

import org.webdav.http.HttpAuth;

public class WebDavHelp {

    public static String getWebDavUrl() {
        /*String url = MApplication.getConfigPreferences().getString("web_dav_url", DEFAULT_WEB_DAV_URL);
        assert url != null;
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;*/
        return "https://dav.jianguoyun.com/dav/";
    }

    public static boolean initWebDav() {
//        String account = MApplication.getConfigPreferences().getString("web_dav_account", "");
//        String password = MApplication.getConfigPreferences().getString("web_dav_password", "");
//        if (!StringUtils.isTrimEmpty(account) && !StringUtils.isTrimEmpty(password)) {
//            HttpAuth.setAuth(account, password);
            HttpAuth.setAuth("1872079@qq.com", "aaaaaa");
            return true;
//        }
//        return false;
    }

    private WebDavHelp() {

    }
}

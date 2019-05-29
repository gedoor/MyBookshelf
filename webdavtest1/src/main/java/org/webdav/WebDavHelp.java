package org.webdav;

import org.webdav.http.WAuth;

public class WebDavHelp {

    public static String getWebDavUrl() {
        /*String url = MApplication.getConfigPreferences().getString("web_dav_url", DEFAULT_WEB_DAV_URL);
        assert url != null;
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;*/
//        return "https://dav.jianguoyun.com/dav";
        return "https://dav.box.com/dav";
//        return "https://d.docs.live.net/E7D70445555B5ACB";
    }

    public static boolean initWebDav() {
//        String account = MApplication.getConfigPreferences().getString("web_dav_account", "");
//        String password = MApplication.getConfigPreferences().getString("web_dav_password", "");
//        if (!StringUtils.isTrimEmpty(account) && !StringUtils.isTrimEmpty(password)) {
//            HttpAuth.setAuth(account, password);
//            WAuth.setAuth("1872079@qq.com", "aaaaaa");
            WAuth.setAuth("moonplusreader@gmail.com", "aa111111");
//            WAuth.setAuth("1872079@qq.com", "th123456");
            return true;
//        }
//        return false;
    }

}

package org.webdav;

import org.webdav.http.WAuth;

public class WebDavHelp {

    public static String getWebDavUrl() {
        return "https://dav.box.com/dav";
    }

    public static boolean initWebDav() {
        WAuth.setAuth("", "");
        return true;
    }

}

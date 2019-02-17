package com.kunfei.bookshelf.help;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.utils.StringUtils;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import static com.kunfei.bookshelf.constant.AppConstant.DEFAULT_WEB_DAV_URL;

public class WebDavHelp {

    public static Sardine getSardine() {
        String account = MApplication.getInstance().getConfigPreferences().getString("web_dav_account", "");
        String password = MApplication.getInstance().getConfigPreferences().getString("web_dav_password", "");
        if (StringUtils.isTrimEmpty(account) || StringUtils.isTrimEmpty(password)) {
            return null;
        }
        Sardine sardine = new OkHttpSardine();
        sardine.setCredentials(account, password);
        return sardine;
    }

    public static String getWebDavUrl() {
        String url = MApplication.getInstance().getConfigPreferences().getString("web_dav_url", DEFAULT_WEB_DAV_URL);
        assert url != null;
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    private WebDavHelp() {

    }
}

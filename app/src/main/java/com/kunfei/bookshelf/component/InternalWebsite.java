package com.kunfei.bookshelf.component;

import com.yanzhenjie.andserver.annotation.Website;
import com.yanzhenjie.andserver.framework.website.AssetsWebsite;

@Website
public class InternalWebsite extends AssetsWebsite {

    public InternalWebsite() {
        super("/web");
    }
}

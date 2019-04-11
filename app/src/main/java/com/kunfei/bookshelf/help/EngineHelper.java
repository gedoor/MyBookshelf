package com.kunfei.bookshelf.help;

import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;
import com.kunfei.bookshelf.utils.StringUtils;

import retrofit2.Response;

public class EngineHelper {

    /**
     * js实现跨域访问,不能删
     */
    @SuppressWarnings("unused")
    public String ajax(String urlStr) {
        try {
            AnalyzeUrl analyzeUrl = new AnalyzeUrl(urlStr);
            Response<String> response = BaseModelImpl.getInstance().getResponseO(analyzeUrl)
                    .blockingFirst();
            return response.body();
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }

    /**
     * js实现解码,不能删
     */
    @SuppressWarnings("unused")
    public String base64Decoder(String base64) {
        return StringUtils.base64Decode(base64);
    }
}

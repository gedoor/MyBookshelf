package com.kunfei.bookshelf.bean;

import android.os.Parcelable;

import com.google.gson.Gson;

import kotlinx.android.parcel.Parcelize;

// 解析阅读3.0的书源规则,toBookSourceBean()方法转换为阅读2.0书源规则
// https://raw.githubusercontent.com/gedoor/legado/master/app/src/main/java/io/legado/app/data/entities/BookSource.kt
public class BookSource3Bean {
    private String bookSourceName = "";                // 名称
    private String bookSourceGroup;            // 分组
    private String bookSourceUrl = "";            // 地址，包括 http/https
    private int bookSourceType = 0;    // 类型，0 文本，1 音频
    private String bookUrlPattern;            // 详情页url正则
    private int customOrder = 0;                      // 手动排序编号
    private Boolean enabled = true;                    // 是否启用
    private Boolean enabledExplore = true;             // 启用发现
    private String header;                     // 请求头
    private String loginUrl;                   // 登录地址
    private String bookSourceComment;            // 注释
    private Long lastUpdateTime = 0L;               // 最后更新时间，用于排序
    private int weight = 0;                            // 智能排序的权重
    private String exploreUrl;                // 发现url
    private ExploreRule ruleExplore;          // 发现规则
    private String searchUrl;                 // 搜索url
    private SearchRule ruleSearch;             // 搜索规则
    private BookInfoRule ruleBookInfo;        // 书籍信息页规则
    private TocRule ruleToc;                 // 目录页规则
    private ContentRule ruleContent;            // 正文页规则


    class ContentRule {
        String content;
        String nextContentUrl;
        String webJs;
        String sourceRegex;
        String replaceRegex;
        String imageStyle;  //默认大小居中,FULL最大宽度
    }

    class TocRule {
        String chapterList;
        String chapterName;
        String chapterUrl;
        String isVip;
        String updateTime;
        String nextTocUrl;
    }

    class BookInfoRule {
        String init;
        String name;
        String author;
        String intro;
        String kind;
        String lastChapter;
        String updateTime;
        String coverUrl;
        String tocUrl;
        String wordCount;
    }

    class SearchRule {
        String bookList;
        String name;
        String author;
        String intro;
        String kind;
        String lastChapter;
        String updateTime;
        String bookUrl;
        String coverUrl;
        String wordCount;
    }

    class ExploreRule {
        String bookList;
        String name;
        String author;
        String intro;
        String kind;
        String lastChapter;
        String updateTime;
        String bookUrl;
        String coverUrl;
        String wordCount;
    }

/*    @Override
    public Object clone() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this);
            return gson.fromJson(json, BookSource3Bean.class);
        } catch (Exception ignored) {
        }
        return this;
    }*/

    class httpRequest {
        String method;
        String body;
        String headers;
        String charset;
    }

    private String searchUrl2RuleSearchUrl(String searchUrl) {
        String RuleSearchUrl = searchUrl;
        if (searchUrl != null) {
            String q = "";
            if (searchUrl.replaceAll("\\s","").matches("^[^,]+,\\{.+\\}")) {
                String[] strings = searchUrl.split(",", 2);
                try {
                    Gson gson = new Gson();
                    httpRequest request = gson.fromJson(strings[1], httpRequest.class);
                    if (request.body != null) {
                        q = request.body
                                .replace("{{key}}", "searchKey")
                                .replaceFirst("\\{\\{([^{}]*)page([^{}]*)\\}\\}", "$1searchPage$2")
                        ;

                        if (request.charset != null)
                            q = q + "|char=" + request.charset;

                        if (request.method != null) {
                            if (request.method.toLowerCase().contains("post"))
                                q = "@" + q;
                            else
                                q = "?" + q;
                        }
                        return strings[0] + q;
                    }

                } catch (Exception ignored) {
                }
            }

            return searchUrl
                    .replace("{{key}}", "searchKey")
                    .replaceFirst("\\{\\{([^{}]*)page([^{}]*)\\}\\}", "$1searchPage$2")
                    ;
        }
        return null;
    }

    public BookSourceBean toBookSourceBean() {
        // 带注释的行，表示2.0/3.0书源json的数据命名不同。注释后方为2.0名称
        String bookSourceType = "";
        if (this.bookSourceType != 0)
            bookSourceType = "" + this.bookSourceType;

        String RuleSearchUrl = searchUrl2RuleSearchUrl(searchUrl);

        return new BookSourceBean(
                bookSourceUrl,
                bookSourceName,
                bookSourceGroup,
                bookSourceType,
                loginUrl,
                lastUpdateTime,
                0, //u  serialNumber,
                weight,
                true, //u enable,
                exploreUrl,//发现规则 ruleFindUrl,
                ruleExplore.bookList,  //  列表 ruleFindList,
                ruleExplore.name,//  ruleFindName,
                ruleExplore.author,//   ruleFindAuthor,
                ruleExplore.kind,//  ruleFindKind,
                ruleExplore.intro,//  ruleFindIntroduce,
                ruleExplore.lastChapter,//    ruleFindLastChapter,
                ruleExplore.coverUrl,//   ruleFindCoverUrl,
                ruleExplore.bookUrl,//???   ruleFindNoteUrl,
                RuleSearchUrl,//   ruleSearchUrl,
                ruleSearch.bookList,//  ruleSearchList,
                ruleSearch.name,// ruleSearchName,
                ruleSearch.author,// ruleSearchAuthor,
                ruleSearch.kind,// ruleSearchKind,
                ruleSearch.intro,// ruleSearchIntroduce,
                ruleSearch.lastChapter,//  ruleSearchLastChapter,
                ruleSearch.coverUrl,//ruleSearchCoverUrl,
                ruleSearch.bookUrl,//  ruleSearchNoteUrl,
                bookUrlPattern, //???  ruleBookUrlPattern,
                ruleBookInfo.init,//      ruleBookInfoInit,
                ruleBookInfo.name,//       ruleBookName,
                ruleBookInfo.author,//       ruleBookAuthor,
                ruleBookInfo.coverUrl,//   ruleCoverUrl,
                ruleBookInfo.intro,//   ruleIntroduce,
                ruleBookInfo.kind,//   ruleBookKind,
                ruleBookInfo.lastChapter,//    ruleBookLastChapter,
                ruleBookInfo.tocUrl,//      ruleChapterUrl,
                ruleToc.nextTocUrl,//       ruleChapterUrlNext,
                ruleToc.chapterList,//   ruleChapterList,
                ruleToc.chapterName,//     ruleChapterName,
                ruleToc.chapterUrl,  // ruleContentUrl,
                ruleContent.nextContentUrl, //ruleContentUrlNext,
                ruleContent.content, //  ruleBookContent,
                ruleContent.replaceRegex,//    ruleBookContentReplace,
                header //  httpUserAgent
        );
    }
}

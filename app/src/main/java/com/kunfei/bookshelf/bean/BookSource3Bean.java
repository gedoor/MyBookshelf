package com.kunfei.bookshelf.bean;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.Transient;

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

    @Transient
    private String userAgent;
    @Transient
    private String RuleSearchUrl;

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

    // 给书源增加一个标签
    public BookSource3Bean addGroupTag(String tag) {
        if (this.bookSourceGroup != null) {
            //为了避免空格、首尾位置的差异造成影响，这里做循环处理
            String[] tags = (this.bookSourceGroup + ";" + tag).split(";");

            List<String> list = new ArrayList<>();
            list.add(tag);
            for (String s : tags) {
                if (!list.contains(s)) {
                    list.add(s);
                }
            }
            bookSourceGroup = tag;
            for (int i = 1; i < list.size(); i++) {
                bookSourceGroup = bookSourceGroup + ";" + list.get(i);
            }
        }
        return this;
    }

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
            if (searchUrl.replaceAll("\\s", "").matches("^[^,]+,\\{.+\\}")) {
                // 正常网址不包含逗号
                String[] strings = searchUrl.split(",", 2);
                try {
                    Gson gson = new Gson();
                    httpRequest request = gson.fromJson(strings[1], httpRequest.class);
                    if (gson.toJson(request).replaceAll("\\s", "").length() > 0) {
                        // 阅读2.0没有单独的header，只有useragent
                        if (request.headers != null) {
                            if (this.header == null)
                                this.header = request.headers;
                            else if (request.headers.trim().length() < 1)
                                this.header = request.headers;
                        }

                        if (request.charset != null) {
                            if (request.charset.trim().length() > 0)
                                q = q + "|char=" + request.charset;
                        }

                        if (request.body != null) {
                            q = request.body
                                    .replace("{{key}}", "searchKey")
                                    .replaceFirst("\\{\\{([^{}]*)page([^{}]*)\\}\\}", "$1searchPage$2")
                                    + q;

                            // post请求的关键词一定在第二部分
                            if (request.method != null) {
                                if (request.method.toLowerCase().contains("post"))
                                    q = "@" + q;
                                else
                                    q = "?" + q;
                            }
                            return strings[0] + q;
                        }

                        RuleSearchUrl = strings[0] + q;
                    } else
                        RuleSearchUrl = searchUrl;

                } catch (Exception e) {
                    e.printStackTrace();
                    RuleSearchUrl = searchUrl;
                }
            }

            return RuleSearchUrl.replaceAll("\\s", "")
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

        RuleSearchUrl = searchUrl2RuleSearchUrl(searchUrl);

        if (header != null && userAgent == null) {
            if (header.matches("(?!).*(User-Agent).*"))
                userAgent = header.replaceFirst("(?!).*(User-Agent)[\\s:]+\"([^\"]+)\".*", "$2");
        }

        String ruleFindUrl=null;
        if(exploreUrl!=null){
            ruleFindUrl=exploreUrl.replaceAll("\\{\\{page\\}\\}","searchPage");
        }

        // 暂时只给发现和搜索添加了header
        String header="";
        if(this.header!=null){
            if(this.header.trim().length()>0)
            header="@Header:"+this.header.replaceAll("\\n"," ");
        }


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
                ruleFindUrl+header,//发现规则 ruleFindUrl,
                ruleExplore.bookList,  //  列表 ruleFindList,
                ruleExplore.name,//  ruleFindName,
                ruleExplore.author,//   ruleFindAuthor,
                ruleExplore.kind,//  ruleFindKind,
                ruleExplore.intro,//  ruleFindIntroduce,
                ruleExplore.lastChapter,//    ruleFindLastChapter,
                ruleExplore.coverUrl,//   ruleFindCoverUrl,
                ruleExplore.bookUrl,//???   ruleFindNoteUrl,
                RuleSearchUrl+header,//   ruleSearchUrl,
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
                userAgent //  httpUserAgent
        );
    }
}

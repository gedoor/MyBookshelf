package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByRegex;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.utils.NetworkUtils;
import com.kunfei.bookshelf.utils.StringUtils;

import org.mozilla.javascript.NativeObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import retrofit2.Response;

import static android.text.TextUtils.isEmpty;

class BookList {
    private String tag;
    private String sourceName;
    private BookSourceBean bookSourceBean;
    private boolean isFind;
    //规则
    private String ruleList;
    private String ruleName;
    private String ruleAuthor;
    private String ruleKind;
    private String ruleIntroduce;
    private String ruleLastChapter;
    private String ruleCoverUrl;
    private String ruleNoteUrl;

    BookList(String tag, String sourceName, BookSourceBean bookSourceBean, boolean isFind) {
        this.tag = tag;
        this.sourceName = sourceName;
        this.bookSourceBean = bookSourceBean;
        this.isFind = isFind;
    }

    Observable<List<SearchBookBean>> analyzeSearchBook(final Response<String> response) {
        return Observable.create(e -> {
            String baseUrl;
            baseUrl = NetworkUtils.getUrl(response);
            if (TextUtils.isEmpty(response.body())) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_web_content_error, baseUrl)));
                return;
            } else {
                Debug.printLog(tag, "┌成功获取搜索结果");
                Debug.printLog(tag, "└" + baseUrl);
            }
            String body = response.body();
            List<SearchBookBean> books = new ArrayList<>();
            AnalyzeRule analyzer = new AnalyzeRule(null);
            analyzer.setContent(body, baseUrl);
            //如果符合详情页url规则
            if (!isEmpty(bookSourceBean.getRuleBookUrlPattern())
                    && baseUrl.matches(bookSourceBean.getRuleBookUrlPattern())) {
                Debug.printLog(tag, ">搜索结果为详情页");
                SearchBookBean item = getItem(analyzer, baseUrl);
                if (item != null) {
                    item.setBookInfoHtml(body);
                    books.add(item);
                }
            } else {
                initRule();
                List<Object> collections;
                boolean reverse = false;
                boolean allInOne = false;
                if (ruleList.startsWith("-")) {
                    reverse = true;
                    ruleList = ruleList.substring(1);
                }
                // 仅使用java正则表达式提取书籍列表
                if (ruleList.startsWith(":")) {
                    ruleList = ruleList.substring(1);
                    Debug.printLog(tag, "┌解析搜索列表");
                    books = getBooksOfRegex(body, ruleList.split("&&"), 0, analyzer);
                } else {
                    if (ruleList.startsWith("+")) {
                        allInOne = true;
                        ruleList = ruleList.substring(1);
                    }
                    //获取列表
                    Debug.printLog(tag, "┌解析搜索列表");
                    collections = analyzer.getElements(ruleList);
                    if (collections.size() == 0 && isEmpty(bookSourceBean.getRuleBookUrlPattern())) {
                        Debug.printLog(tag, "└搜索列表为空,当做详情页处理");
                        SearchBookBean item = getItem(analyzer, baseUrl);
                        if (item != null) {
                            item.setBookInfoHtml(body);
                            books.add(item);
                        }
                    } else {
                        Debug.printLog(tag, "└找到 " + collections.size() + " 个匹配的结果");
                        if (allInOne) {
                            for (int i = 0; i < collections.size(); i++) {
                                Object object = collections.get(i);
                                SearchBookBean item = getItemAllInOne(analyzer, object, baseUrl, i == 0);
                                if (item != null) {
                                    //如果网址相同则缓存
                                    if (baseUrl.equals(item.getNoteUrl())) {
                                        item.setBookInfoHtml(body);
                                    }
                                    books.add(item);
                                }
                            }
                        } else {
                            for (int i = 0; i < collections.size(); i++) {
                                Object object = collections.get(i);
                                analyzer.setContent(object, baseUrl);
                                SearchBookBean item = getItemInList(analyzer, baseUrl, i == 0);
                                if (item != null) {
                                    //如果网址相同则缓存
                                    if (baseUrl.equals(item.getNoteUrl())) {
                                        item.setBookInfoHtml(body);
                                    }
                                    books.add(item);
                                }
                            }
                        }
                    }
                }
                if (books.size() > 1 && reverse) {
                    Collections.reverse(books);
                }
            }
            if (books.isEmpty()) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.no_book_name)));
                return;
            }
            Debug.printLog(tag, "-书籍列表解析结束");
            e.onNext(books);
            e.onComplete();
        });
    }

    private void initRule() {
        if (isFind && !TextUtils.isEmpty(bookSourceBean.getRuleFindList())) {
            ruleList = bookSourceBean.getRuleFindList();
            ruleName = bookSourceBean.getRuleFindName();
            ruleAuthor = bookSourceBean.getRuleFindAuthor();
            ruleKind = bookSourceBean.getRuleFindKind();
            ruleIntroduce = bookSourceBean.getRuleFindIntroduce();
            ruleCoverUrl = bookSourceBean.getRuleFindCoverUrl();
            ruleLastChapter = bookSourceBean.getRuleFindLastChapter();
            ruleNoteUrl = bookSourceBean.getRuleFindNoteUrl();
        } else {
            ruleList = bookSourceBean.getRuleSearchList();
            ruleName = bookSourceBean.getRuleSearchName();
            ruleAuthor = bookSourceBean.getRuleSearchAuthor();
            ruleKind = bookSourceBean.getRuleSearchKind();
            ruleIntroduce = bookSourceBean.getRuleSearchIntroduce();
            ruleCoverUrl = bookSourceBean.getRuleSearchCoverUrl();
            ruleLastChapter = bookSourceBean.getRuleSearchLastChapter();
            ruleNoteUrl = bookSourceBean.getRuleSearchNoteUrl();
        }
    }

    /**
     * 详情页
     */
    private SearchBookBean getItem(AnalyzeRule analyzer, String baseUrl) throws Exception {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        item.setTag(tag);
        item.setOrigin(sourceName);
        item.setNoteUrl(baseUrl);
        // 获取详情页预处理规则
        String ruleInfoInit = bookSourceBean.getRuleBookInfoInit();
        if (!isEmpty(ruleInfoInit)) {
            // 仅使用java正则表达式提取书籍详情
            if (ruleInfoInit.startsWith(":")) {
                ruleInfoInit = ruleInfoInit.substring(1);
                Debug.printLog(tag, "┌详情信息预处理");
                BookShelfBean bookShelfBean = new BookShelfBean();
                bookShelfBean.setTag(tag);
                bookShelfBean.setNoteUrl(baseUrl);
                AnalyzeByRegex.getInfosOfRegex(String.valueOf(analyzer.getContent()), ruleInfoInit.split("&&"), 0, bookShelfBean, analyzer, bookSourceBean, tag);
                item.setName(bookShelfBean.getBookInfoBean().getName());
                item.setAuthor(bookShelfBean.getBookInfoBean().getAuthor());
                item.setCoverUrl(bookShelfBean.getBookInfoBean().getCoverUrl());
                item.setLastChapter(bookShelfBean.getLastChapterName());
                item.setIntroduce(bookShelfBean.getBookInfoBean().getIntroduce());
                return item;
            } else {
                Object object = analyzer.getElement(ruleInfoInit);
                if (object != null) {
                    analyzer.setContent(object);
                }
            }
        }
        Debug.printLog(tag, ">书籍网址:" + baseUrl);
        Debug.printLog(tag, "┌获取书名");
        String bookName = StringUtils.formatHtml(analyzer.getString(bookSourceBean.getRuleBookName()));
        Debug.printLog(tag, "└" + bookName);
        if (!TextUtils.isEmpty(bookName)) {
            item.setName(bookName);
            Debug.printLog(tag, "┌获取作者");
            item.setAuthor(StringUtils.formatHtml(analyzer.getString(bookSourceBean.getRuleBookAuthor())));
            Debug.printLog(tag, "└" + item.getAuthor());
            Debug.printLog(tag, "┌获取封面");
            item.setCoverUrl(analyzer.getString(bookSourceBean.getRuleCoverUrl(), true));
            Debug.printLog(tag, "└" + item.getCoverUrl());
            Debug.printLog(tag, "┌获取分类");
            item.setKind(StringUtils.join(",", analyzer.getStringList(bookSourceBean.getRuleBookKind())));
            Debug.printLog(tag, "└" + item.getKind());
            Debug.printLog(tag, "┌获取最新章节");
            item.setLastChapter(analyzer.getString(bookSourceBean.getRuleBookLastChapter()));
            Debug.printLog(tag, "└最新章节:" + item.getLastChapter());
            Debug.printLog(tag, "┌获取简介");
            item.setIntroduce(analyzer.getString(bookSourceBean.getRuleIntroduce()));
            Debug.printLog(tag, "└" + item.getIntroduce(), true, true);
            return item;
        }
        return null;
    }

    private SearchBookBean getItemAllInOne(AnalyzeRule analyzer, Object object, String baseUrl, boolean printLog) {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        NativeObject nativeObject = (NativeObject) object;
        Debug.printLog(tag, "┌获取书名", printLog);
        String bookName = StringUtils.formatHtml(String.valueOf(nativeObject.get(ruleName)));
        Debug.printLog(tag, "└" + bookName, printLog);
        if (!isEmpty(bookName)) {
            item.setTag(tag);
            item.setOrigin(sourceName);
            item.setName(bookName);
            Debug.printLog(tag, "┌获取作者", printLog);
            item.setAuthor(StringUtils.formatHtml(String.valueOf(nativeObject.get(ruleAuthor))));
            Debug.printLog(tag, "└" + item.getAuthor(), printLog);
            Debug.printLog(tag, "┌获取分类", printLog);
            item.setKind(StringUtils.join(",", String.valueOf(nativeObject.get(ruleKind))));
            Debug.printLog(tag, "└" + item.getKind(), printLog);
            Debug.printLog(tag, "┌获取最新章节", printLog);
            item.setLastChapter(String.valueOf(nativeObject.get(ruleLastChapter)));
            Debug.printLog(tag, "└" + item.getLastChapter(), printLog);
            Debug.printLog(tag, "┌获取简介", printLog);
            item.setIntroduce(String.valueOf(nativeObject.get(ruleIntroduce)));
            Debug.printLog(tag, "└" + item.getIntroduce(), printLog, true);
            Debug.printLog(tag, "┌获取封面", printLog);
            if (!isEmpty(ruleCoverUrl))
                item.setCoverUrl(NetworkUtils.getAbsoluteURL(baseUrl, String.valueOf(nativeObject.get(ruleCoverUrl))));
            Debug.printLog(tag, "└" + item.getCoverUrl(), printLog);
            Debug.printLog(tag, "┌获取书籍网址", printLog);
            String resultUrl = String.valueOf(nativeObject.get(ruleNoteUrl));
            if (isEmpty(resultUrl)) resultUrl = baseUrl;
            item.setNoteUrl(resultUrl);
            Debug.printLog(tag, "└" + item.getNoteUrl(), printLog);
            return item;
        }
        return null;
    }

    private SearchBookBean getItemInList(AnalyzeRule analyzer, String baseUrl, boolean printLog) throws
            Exception {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        Debug.printLog(tag, "┌获取书名", printLog);
        String bookName = StringUtils.formatHtml(analyzer.getString(ruleName));
        Debug.printLog(tag, "└" + bookName, printLog);
        if (!TextUtils.isEmpty(bookName)) {
            item.setTag(tag);
            item.setOrigin(sourceName);
            item.setName(bookName);
            Debug.printLog(tag, "┌获取作者", printLog);
            item.setAuthor(StringUtils.formatHtml(analyzer.getString(ruleAuthor)));
            Debug.printLog(tag, "└" + item.getAuthor(), printLog);
            Debug.printLog(tag, "┌获取分类", printLog);
            item.setKind(StringUtils.join(",", analyzer.getStringList(ruleKind)));
            Debug.printLog(tag, "└" + item.getKind(), printLog);
            Debug.printLog(tag, "┌获取最新章节", printLog);
            item.setLastChapter(analyzer.getString(ruleLastChapter));
            Debug.printLog(tag, "└" + item.getLastChapter(), printLog);
            Debug.printLog(tag, "┌获取简介", printLog);
            item.setIntroduce(analyzer.getString(ruleIntroduce));
            Debug.printLog(tag, "└" + item.getIntroduce(), printLog, true);
            Debug.printLog(tag, "┌获取封面", printLog);
            item.setCoverUrl(analyzer.getString(ruleCoverUrl, true));
            Debug.printLog(tag, "└" + item.getCoverUrl(), printLog);
            Debug.printLog(tag, "┌获取书籍网址", printLog);
            String resultUrl = analyzer.getString(ruleNoteUrl, true);
            if (isEmpty(resultUrl)) resultUrl = baseUrl;
            item.setNoteUrl(resultUrl);
            Debug.printLog(tag, "└" + item.getNoteUrl(), printLog);
            return item;
        }
        return null;
    }

    // 纯java模式正则表达式获取书籍列表
    private List<SearchBookBean> getBooksOfRegex(String res, String[] regs,
                                                 int index, AnalyzeRule analyzer) throws Exception {
        Matcher resM = Pattern.compile(regs[index]).matcher(res);
        String baseUrl = analyzer.getBaseUrl();
        // 判断规则是否有效,当搜索列表规则无效时当作详情页处理
        if (!resM.find()) {
            List<SearchBookBean> books = new ArrayList<>();
            SearchBookBean bookBean = getItem(analyzer, baseUrl);
            books.add(bookBean);
            return books;
        }
        // 判断索引的规则是最后一个规则
        if (index + 1 == regs.length) {
            // 创建书籍信息缓存数组
            List<SearchBookBean> books = new ArrayList<>();
            // 获取规则列表
            String[] ruleList = new String[]{
                    ruleName,       // 获取书名规则
                    ruleAuthor,     // 获取作者规则
                    ruleKind,       // 获取分类规则
                    ruleLastChapter,// 获取终章规则
                    ruleIntroduce,  // 获取简介规则
                    ruleCoverUrl,   // 获取封面规则
                    ruleNoteUrl     // 获取详情规则
            };
            // 分离规则参数
            List<List<String>> ruleParams = new ArrayList();    // 创建规则参数容器
            List<Boolean> hasVars = new ArrayList<>();          // 创建put&get参数标志容器
            for (int i = ruleList.length; i-- > 0; ) {
                String rule = ruleList[i];
                ruleParams.add(0, AnalyzeByRegex.splitRegexRule(rule));
                hasVars.add(0, rule.contains("@put") || rule.contains("@get"));
            }
            // 提取正则参数
            List<List<Integer>> ruleGroups = new ArrayList();
            for (int i = ruleParams.size(); i-- > 0; ) {
                List<String> ruleParam = ruleParams.get(i);
                List<Integer> ruleGroup = new ArrayList();
                for (int j = ruleParam.size(); j-- > 0; ) {
                    ruleGroup.add(0, ruleParam.get(j).charAt(0) == '$' ? AnalyzeByRegex.string2Int(ruleParam.get(j)) : -1);
                }
                ruleGroups.add(0, ruleGroup);
            }
            // 创建结果缓存
            List<String> infoList = new ArrayList();
            // 提取书籍列表
            do {
                // 新建书籍容器
                SearchBookBean item = new SearchBookBean(tag, sourceName);
                analyzer.setBook(item);

                infoList.clear();
                StringBuilder infoVal = new StringBuilder();
                for (int i = ruleParams.size(); i-- > 0; ) {
                    List<String> ruleParam = ruleParams.get(i);
                    List<Integer> ruleGroup = ruleGroups.get(i);
                    infoVal.setLength(0);
                    for (int j = ruleParam.size(); j-- > 0; ) {
                        if (ruleGroup.get(j) != -1) {
                            infoVal.insert(0, resM.group(ruleGroup.get(j)));
                        } else {
                            infoVal.insert(0, ruleParam.get(j));
                        }
                    }
                    infoList.add(0, hasVars.get(i) ? AnalyzeByRegex.checkKeys(infoVal.toString(), analyzer) : infoVal.toString());
                }
                // 保存当前节点的书籍信息
                item.setSearchInfo(
                        infoList.get(0), // 保存书名
                        infoList.get(1), // 保存作者
                        infoList.get(2), // 保存分类
                        infoList.get(3), // 保存终章
                        infoList.get(4), // 保存简介
                        infoList.get(5), // 保存封面
                        infoList.get(6)  // 保存详情
                );
                books.add(item);
                // 判断搜索结果是否为详情页
                if (books.size() == 1 && (isEmpty(infoList.get(6)) || infoList.get(6).equals(baseUrl))) {
                    books.get(0).setNoteUrl(baseUrl);
                    books.get(0).setBookInfoHtml(res);
                    return books;
                }
            } while (resM.find());

            Debug.printLog(tag, "└找到 " + books.size() + " 个匹配的结果");
            Debug.printLog(tag, "┌获取书籍名称");
            Debug.printLog(tag, "└" + books.get(0).getName());
            Debug.printLog(tag, "┌获取作者名称");
            Debug.printLog(tag, "└" + books.get(0).getAuthor());
            Debug.printLog(tag, "┌获取分类信息");
            Debug.printLog(tag, "└" + books.get(0).getKind());
            Debug.printLog(tag, "┌获取最新章节");
            Debug.printLog(tag, "└" + books.get(0).getLastChapter());
            Debug.printLog(tag, "┌获取简介内容");
            Debug.printLog(tag, "└" + books.get(0).getIntroduce(), true, true);
            Debug.printLog(tag, "┌获取封面网址");
            Debug.printLog(tag, "└" + books.get(0).getCoverUrl());
            Debug.printLog(tag, "┌获取书籍网址");
            Debug.printLog(tag, "└" + books.get(0).getNoteUrl());
            return books;
        } else {
            StringBuilder result = new StringBuilder();
            do {
                result.append(resM.group());
            } while (resM.find());
            return getBooksOfRegex(result.toString(), regs, ++index, analyzer);
        }
    }
}
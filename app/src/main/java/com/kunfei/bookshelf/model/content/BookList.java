package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
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
    private String name;
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

    BookList(String tag, String name, BookSourceBean bookSourceBean, boolean isFind) {
        this.tag = tag;
        this.name = name;
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
                    Debug.printLog(tag, "┌解析搜索列表");
                    //获取列表
                    Debug.printLog(tag, "┌解析搜索列表");
                    collections = analyzer.getElements(ruleList);
                    if (collections.size() == 0) {
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

    private SearchBookBean getItem(AnalyzeRule analyzer, String baseUrl) throws Exception {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        Debug.printLog(tag, ">书籍网址:" + baseUrl);
        Debug.printLog(tag, "┌获取书名");
        String bookName = analyzer.getString(bookSourceBean.getRuleBookName());
        Debug.printLog(tag, "└" + bookName);
        if (!TextUtils.isEmpty(bookName)) {
            item.setTag(tag);
            item.setOrigin(name);
            item.setNoteUrl(baseUrl);
            item.setName(bookName);
            Debug.printLog(tag, "┌获取作者");
            item.setAuthor(analyzer.getString(bookSourceBean.getRuleBookAuthor()));
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
        String bookName = String.valueOf(nativeObject.get(ruleName));
        Debug.printLog(tag, "└" + bookName, printLog);
        if (!isEmpty(bookName)) {
            item.setTag(tag);
            item.setOrigin(name);
            item.setName(bookName);
            Debug.printLog(tag, "┌获取作者", printLog);
            item.setAuthor(String.valueOf(nativeObject.get(ruleAuthor)));
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

    private SearchBookBean getItemInList(AnalyzeRule analyzer, String baseUrl, boolean printLog) throws Exception {
        SearchBookBean item = new SearchBookBean();
        analyzer.setBook(item);
        Debug.printLog(tag, "┌获取书名", printLog);
        String bookName = analyzer.getString(ruleName);
        Debug.printLog(tag, "└" + bookName, printLog);
        if (!TextUtils.isEmpty(bookName)) {
            item.setTag(tag);
            item.setOrigin(name);
            item.setName(bookName);
            Debug.printLog(tag, "┌获取作者", printLog);
            item.setAuthor(analyzer.getString(ruleAuthor));
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

    // region 纯Java代码解析文本内容,模块代码
    // 纯java模式正则表达式获取书籍列表
    private List<SearchBookBean> getBooksOfRegex(String res, String[] regs, int index, AnalyzeRule analyzer) {
        Matcher resM = Pattern.compile(regs[index]).matcher(res);
        String baseUrl = analyzer.getBaseUrl();
        // 判断规则是否有效,当搜索列表规则无效时当作详情页处理
        if (!resM.find()) {
            List<SearchBookBean> books = new ArrayList<>();
            SearchBookBean bookBean = new SearchBookBean();
            bookBean.setNoteUrl(baseUrl);
            bookBean.setBookInfoHtml(res);
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
            // 创建put&get参数判断容器
            List<Boolean> hasVars = new ArrayList<>();
            // 创建拆分规则容器
            List<String[]> ruleGroups = new ArrayList<>();
            // 提取规则信息
            for (String rule : ruleList) {
                ruleGroups.add(splitRegexRule(rule));
                hasVars.add(rule.contains("@put") || rule.contains("@get"));
            }
            // 提取书籍列表信息
            do {
                // 获取列表规则分组数
                int resCount = resM.groupCount();
                // 新建书籍容器
                SearchBookBean item = new SearchBookBean(tag, name);
                analyzer.setBook(item);
                // 新建规则结果容器
                String[] infoList = new String[ruleList.length];
                // 合并规则结果内容
                for (int i = 0; i < infoList.length; i++) {
                    StringBuilder infoVal = new StringBuilder();
                    for (String ruleGroup : ruleGroups.get(i)) {
                        if (ruleGroup.startsWith("$")) {
                            int groupIndex = string2Int(ruleGroup);
                            if (groupIndex <= resCount) {
                                infoVal.append(StringUtils.trim(resM.group(groupIndex)));
                                continue;
                            }
                        }
                        infoVal.append(ruleGroup);
                    }
                    infoList[i] = hasVars.get(i) ? checkKeys(infoVal.toString(), analyzer) : infoVal.toString();
                }
                // 保存当前节点的书籍信息
                item.setSearchInfo(
                        infoList[0], // 保存书名
                        infoList[1], // 保存作者
                        infoList[2], // 保存分类
                        infoList[3], // 保存终章
                        infoList[4], // 保存简介
                        NetworkUtils.getAbsoluteURL(baseUrl, infoList[5]), // 保存封面
                        infoList[6]  // 保存详情
                );
                books.add(item);
                // 判断搜索结果是否为详情页
                if (books.size() == 1 && (isEmpty(infoList[6]) || infoList[6].equals(baseUrl))) {
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

    // 拆分正则表达式替换规则(如:$\d和$\d\d) /*注意:千万别用正则表达式拆分字符串,效率太低了!*/
    private static String[] splitRegexRule(String str) {
        int start = 0, index = 0, len = str.length();
        List<String> arr = new ArrayList<>();
        while (start < len) {
            if ((str.charAt(start) == '$') && (str.charAt(start + 1) >= '0') && (str.charAt(start + 1) <= '9')) {
                if (start > index) arr.add(str.substring(index, start));
                if ((start + 2 < len) && (str.charAt(start + 2) >= '0') && (str.charAt(start + 2) <= '9')) {
                    arr.add(str.substring(start, start + 3));
                    index = start += 3;
                } else {
                    arr.add(str.substring(start, start + 2));
                    index = start += 2;
                }
            } else {
                ++start;
            }
        }
        if (start > index) arr.add(str.substring(index, start));
        return arr.toArray(new String[0]);
    }

    // 存取字符串中的put&get参数
    private String checkKeys(String str, AnalyzeRule analyzer) {
        if (str.contains("@put:{")) {
            Matcher putMatcher = Pattern.compile("@put:\\{([^,]*):([^\\}]*)\\}").matcher(str);
            while (putMatcher.find()) {
                str = str.replace(putMatcher.group(0), "");
                analyzer.put(putMatcher.group(1), putMatcher.group(2));
            }
        }
        if (str.contains("@get:{")) {
            Matcher getMatcher = Pattern.compile("@get:\\{([^\\}]*)\\}").matcher(str);
            while (getMatcher.find()) {
                str = str.replace(getMatcher.group(), analyzer.get(getMatcher.group(1)));
            }
        }
        return str;
    }

    // String数字转int数字的高效方法(利用ASCII值判断)
    private static int string2Int(String s) {
        int r = 0;
        char n;
        for (int i = 0, l = s.length(); i < l; i++) {
            n = s.charAt(i);
            if (n >= '0' && n <= '9') {
                r = r * 10 + (n - 0x30); //'0-9'的ASCII值为0x30-0x39
            }
        }
        return r;
    }
    // endregion
}
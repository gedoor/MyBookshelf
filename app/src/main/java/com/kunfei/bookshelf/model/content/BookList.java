package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.SearchBookBean;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.utils.StringUtils;

import org.mozilla.javascript.NativeObject;

import java.util.*;
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
    private String body;
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
            okhttp3.Response networkResponse = response.raw().networkResponse();
            if (networkResponse != null) {
                baseUrl = networkResponse.request().url().toString();
            } else {
                baseUrl = response.raw().request().url().toString();
            }
            if (TextUtils.isEmpty(response.body())) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_web_content_error, baseUrl)));
                return;
            } else {
                Debug.printLog(tag, "┌成功获取搜索结果");
                Debug.printLog(tag, "└" + baseUrl);
            }
            List<SearchBookBean> books = new ArrayList<>();
            AnalyzeRule analyzer = new AnalyzeRule(null);
            body = response.body();
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
                if (ruleList.startsWith(":")){
                    ruleList = ruleList.substring(1);
                    Debug.printLog(tag, "┌解析搜索列表");
                    books = getItemsOfRegex(body, ruleList.split("&&"), 0, baseUrl);
                }
                else {
                    // 使用AllInOne规则模式提取目录列表
                    if (ruleList.startsWith("+")) {
                        allInOne = true;
                        ruleList = ruleList.substring(1);
                    }
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
                                SearchBookBean item = getItemAllInOne(object, baseUrl, i == 0);
                                if (item != null) {
                                    books.add(item);
                                }
                            }
                        } else {
                            for (int i = 0; i < collections.size(); i++) {
                                Object object = collections.get(i);
                                analyzer.setContent(object, baseUrl);
                                SearchBookBean item = getItemInList(analyzer, baseUrl, i == 0);
                                if (item != null) {
                                    books.add(item);
                                }
                            }
                        }
                        if (books.size() > 1 && reverse) {
                            Collections.reverse(books);
                        }
                    }
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
            item.setCoverUrl(analyzer.getString(bookSourceBean.getRuleCoverUrl()));
            Debug.printLog(tag, "└" + item.getCoverUrl());
            Debug.printLog(tag, "┌获取分类");
            item.setKind(StringUtils.join(",", analyzer.getStringList(bookSourceBean.getRuleBookKind())));
            Debug.printLog(tag, "└" + item.getKind());
            Debug.printLog(tag, "┌获取最新章节");
            item.setLastChapter(analyzer.getString(bookSourceBean.getRuleBookLastChapter()));
            Debug.printLog(tag, "└最新章节:" + item.getLastChapter());
            Debug.printLog(tag, "┌获取简介");
            item.setIntroduce(analyzer.getString(bookSourceBean.getRuleIntroduce()));
            Debug.printLog(tag, "└" + item.getIntroduce());
            return item;
        }
        return null;
    }

    private SearchBookBean getItemAllInOne(Object object, String baseUrl, boolean printLog) {
        SearchBookBean item = new SearchBookBean();
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
            Debug.printLog(tag, "└" + item.getIntroduce(), printLog);
            Debug.printLog(tag, "┌获取封面", printLog);
            item.setCoverUrl(String.valueOf(nativeObject.get(ruleCoverUrl)));
            Debug.printLog(tag, "└" + item.getCoverUrl(), printLog);
            Debug.printLog(tag, "┌获取书籍网址", printLog);
            String resultUrl = String.valueOf(nativeObject.get(ruleNoteUrl));
            if (isEmpty(resultUrl)) {
                //详情页等于搜索页
                resultUrl = baseUrl;
                item.setBookInfoHtml(body);
            }
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
            Debug.printLog(tag, "└" + item.getIntroduce(), printLog);
            Debug.printLog(tag, "┌获取封面", printLog);
            item.setCoverUrl(analyzer.getString(ruleCoverUrl, true));
            Debug.printLog(tag, "└" + item.getCoverUrl(), printLog);
            Debug.printLog(tag, "┌获取书籍网址", printLog);
            String resultUrl = analyzer.getString(ruleNoteUrl, true);
            if (isEmpty(resultUrl)) {
                //详情页等于搜索页
                resultUrl = baseUrl;
                item.setBookInfoHtml(body);
            }
            item.setNoteUrl(resultUrl);
            Debug.printLog(tag, "└" + item.getNoteUrl(), printLog);
            return item;
        }
        return null;
    }

    // 纯java模式正则表达式获取书籍列表
    private List<SearchBookBean> getItemsOfRegex(String res, String[] regs, int index, String baseUrl){
        Matcher resM = Pattern.compile(regs[index]).matcher(res);
        // 判断规则是否有效,当搜索列表规则无效时当作详情页处理
        if (!resM.find()){
            List<SearchBookBean> books = new ArrayList<>(new ArrayList<>());
            books.get(0).setNoteUrl(baseUrl);
            books.get(0).setBookInfoHtml(res);
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
            // 创建表达式拆分缓存
            Matcher[] ruleMatchers = new Matcher[ruleList.length];
            // 创建表达式索引缓存
            List<List<Integer>> ruleKeys = new ArrayList<>();
            // 获取规则表达式
            String valReg = "\\$(\\d+)"; // 正则参数提取规则
            for(int i=0, len = ruleList.length; i<len; ++i){
                ruleMatchers[i] = Pattern.compile(valReg).matcher(ruleList[i]);
                ruleKeys.add(new ArrayList<>());
                while (ruleMatchers[i].find()){
                    ruleKeys.get(i).add(Integer.parseInt(ruleMatchers[i].group(1)));
                }
            }
            // 提取书籍列表信息
            do{
                String[] tmpList = ruleList.clone();
                for(int i=0, len = tmpList.length; i<len; ++i){
                    for(Integer valKey : ruleKeys.get(i)){
                        tmpList[i]=tmpList[i].replaceFirst(valReg, resM.group(valKey));
                    }
                }
                // 保存当前节点的书籍信息
                books.add(new SearchBookBean(
                        tag,
                        name,
                        tmpList[0].replaceAll("^[\\n\\s]*|[\\n\\s]*$",""), // 保存书名
                        tmpList[1].replaceAll("^[\\n\\s]*|[\\n\\s]*$",""), // 保存作者
                        tmpList[2].replaceAll("^[\\n\\s]*|[\\n\\s]*$",""), // 保存分类
                        tmpList[3].replaceAll("^[\\n\\s]*|[\\n\\s]*$",""), // 保存终章
                        tmpList[4].replaceAll("^[\\n\\s]*|[\\n\\s]*$",""), // 保存简介
                        tmpList[5], // 保存封面
                        tmpList[6]  // 保存详情
                ));
                // 判断搜索结果是否为详情页
                if(books.size() == 1 && (isEmpty(ruleNoteUrl) || ruleNoteUrl.equals(baseUrl)))
                {
                    books.get(0).setNoteUrl(baseUrl);
                    books.get(0).setBookInfoHtml(res);
                    return books;
                }
            }while (resM.find());
            Debug.printLog(tag, "└找到 " + books.size() + " 个匹配的结果");
            Debug.printLog(tag, "┌获取书名");
            Debug.printLog(tag, "└" + books.get(0).getName());
            Debug.printLog(tag, "┌获取作者");
            Debug.printLog(tag, "└" + books.get(0).getAuthor());
            Debug.printLog(tag, "┌获取分类");
            Debug.printLog(tag, "└" + books.get(0).getKind());
            Debug.printLog(tag, "┌获取最新章节");
            Debug.printLog(tag, "└" + books.get(0).getKind());
            Debug.printLog(tag, "┌获取简介");
            Debug.printLog(tag, "└" + books.get(0).getIntroduce());
            Debug.printLog(tag, "┌获取封面");
            Debug.printLog(tag, "└" + books.get(0).getCoverUrl());
            Debug.printLog(tag, "┌获取书籍网址");
            Debug.printLog(tag, "└" + books.get(0).getNoteUrl());
            return books;
        }
        else{
            StringBuilder result = new StringBuilder();
            do{ result.append(resM.group()); }while (resM.find());
            return getItemsOfRegex(result.toString(), regs, ++index, baseUrl);
        }
    }
}
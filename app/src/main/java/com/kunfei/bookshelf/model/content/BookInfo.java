package com.kunfei.bookshelf.model.content;

import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.utils.NetworkUtils;
import com.kunfei.bookshelf.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

class BookInfo {
    private String tag;
    private String name;
    private BookSourceBean bookSourceBean;

    BookInfo(String tag, String name, BookSourceBean bookSourceBean) {
        this.tag = tag;
        this.name = name;
        this.bookSourceBean = bookSourceBean;
    }

    Observable<BookShelfBean> analyzeBookInfo(String s, final BookShelfBean bookShelfBean) {
        return Observable.create(e -> {
            String baseUrl = bookShelfBean.getNoteUrl();

            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_book_info_error) + baseUrl));
                return;
            } else {
                Debug.printLog(tag, "┌成功获取详情页");
                Debug.printLog(tag, "└" + baseUrl);
            }
            bookShelfBean.setTag(tag);

            BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
            bookInfoBean.setNoteUrl(baseUrl);   //id
            bookInfoBean.setTag(tag);
            bookInfoBean.setOrigin(name);
            bookInfoBean.setBookSourceType(bookSourceBean.getBookSourceType()); // 是否为有声读物

            AnalyzeRule analyzer = new AnalyzeRule(bookShelfBean);
            analyzer.setContent(s, baseUrl);

            // 获取详情页预处理规则
            String ruleInfoInit = bookSourceBean.getRuleBookInfoInit();
            boolean isRegex = false;
            if (!isEmpty(ruleInfoInit)) {
                // 仅使用java正则表达式提取书籍详情
                if (ruleInfoInit.startsWith(":")) {
                    isRegex = true;
                    ruleInfoInit = ruleInfoInit.substring(1);
                    Debug.printLog(tag, "┌详情信息预处理");
                    getInfosOfRegex(s, ruleInfoInit.split("&&"), 0, bookShelfBean, analyzer);
                } else {
                    Object object = analyzer.getElement(ruleInfoInit);
                    if (object != null) {
                        analyzer.setContent(object);
                    }
                }
            }
            if (!isRegex) {
                Debug.printLog(tag, "┌详情信息预处理");
                Object object = analyzer.getElement(ruleInfoInit);
                if (object != null) analyzer.setContent(object);
                Debug.printLog(tag, "└详情预处理完成");

                Debug.printLog(tag, "┌获取书名");
                String bookName = analyzer.getString(bookSourceBean.getRuleBookName());
                if (!isEmpty(bookName)) bookInfoBean.setName(bookName);
                Debug.printLog(tag, "└" + bookName);

                Debug.printLog(tag, "┌获取作者");
                String bookAuthor = analyzer.getString(bookSourceBean.getRuleBookAuthor());
                if (!isEmpty(bookAuthor)) bookInfoBean.setAuthor(bookAuthor);
                Debug.printLog(tag, "└" + bookAuthor);

                Debug.printLog(tag, "┌获取分类");
                String bookKind = analyzer.getString(bookSourceBean.getRuleBookKind());
                Debug.printLog(tag, "└" + bookKind);

                Debug.printLog(tag, "┌获取最新章节");
                String bookLastChapter = analyzer.getString(bookSourceBean.getRuleBookLastChapter());
                if (!isEmpty(bookLastChapter)) bookShelfBean.setLastChapterName(bookLastChapter);
                Debug.printLog(tag, "└" + bookLastChapter);

                Debug.printLog(tag, "┌获取简介");
                String bookIntroduce = analyzer.getString(bookSourceBean.getRuleIntroduce());
                if (!isEmpty(bookIntroduce)) bookInfoBean.setIntroduce(bookIntroduce);
                Debug.printLog(tag, "└" + bookIntroduce, true, true);

                Debug.printLog(tag, "┌获取封面");
                String bookCoverUrl = analyzer.getString(bookSourceBean.getRuleCoverUrl(), true);
                if (!isEmpty(bookCoverUrl)) bookInfoBean.setCoverUrl(bookCoverUrl);
                Debug.printLog(tag, "└" + bookCoverUrl);

                Debug.printLog(tag, "┌获取目录网址");
                String bookCatalogUrl = analyzer.getString(bookSourceBean.getRuleChapterUrl(), true);
                if (isEmpty(bookCatalogUrl)) bookCatalogUrl = baseUrl;
                bookInfoBean.setChapterUrl(bookCatalogUrl);
                //如果目录页和详情页相同,暂存页面内容供获取目录用
                if (bookCatalogUrl.equals(baseUrl)) bookInfoBean.setChapterListHtml(s);
                Debug.printLog(tag, "└" + bookInfoBean.getChapterUrl());
                bookShelfBean.setBookInfoBean(bookInfoBean);
                Debug.printLog(tag, "-详情页解析完成");
            }
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }


    // region 纯Java代码解析文本内容,模块代码
    // 纯java模式正则表达式获取书籍详情信息
    private void getInfosOfRegex(String res, String[] regs, int index,
                                 BookShelfBean bookShelfBean,AnalyzeRule analyzer){
        BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
        String baseUrl = bookShelfBean.getNoteUrl();
        Matcher resM = Pattern.compile(regs[index]).matcher(res);
        // 判断规则是否有效,当搜索列表规则无效时跳过详情页处理
        if (!resM.find()){
            Debug.printLog(tag, "└详情预处理失败,跳过详情页解析");
            Debug.printLog(tag, "┌获取目录网址");
            bookInfoBean.setChapterUrl(baseUrl);
            bookInfoBean.setChapterListHtml(res);
            Debug.printLog(tag, "└" + baseUrl);
            return;
        }
        // 判断索引的规则是最后一个规则
        if (index + 1 == regs.length) {
            // 获取规则列表
            String[] ruleList = new String[]{
                    bookSourceBean.getRuleBookName(),       // 获取书名规则
                    bookSourceBean.getRuleBookAuthor(),     // 获取作者规则
                    bookSourceBean.getRuleBookKind(),       // 获取分类规则
                    bookSourceBean.getRuleBookLastChapter(),// 获取终章规则
                    bookSourceBean.getRuleIntroduce(),      // 获取简介规则
                    bookSourceBean.getRuleCoverUrl(),       // 获取封面规则
                    bookSourceBean.getRuleChapterUrl()      // 获取目录规则
            };
            // 创建put&get参数判断容器
            List<Boolean> hasVars = new ArrayList<>();
            // 创建拆分规则容器
            List<String[]> ruleGroups = new ArrayList<>();
            // 提取规则信息
            for(String rule:ruleList){
                ruleGroups.add(splitRegexRule(rule));
                hasVars.add(rule.contains("@put") || rule.contains("@get"));
            }
            // 获取详情规则分组数
            int resCount = resM.groupCount();
            // 新建规则结果容器
            String[] infoList = new String[ruleList.length];
            // 合并规则结果内容
            for(int i=0; i<infoList.length; i++){
                StringBuilder infoVal = new StringBuilder();
                for(String ruleGroup:ruleGroups.get(i)) {
                    if(ruleGroup.startsWith("$")){
                        int groupIndex = string2Int(ruleGroup);
                        if(groupIndex <= resCount){
                            infoVal.append(StringUtils.trim(resM.group(groupIndex)));
                            continue;
                        }
                    }
                    infoVal.append(ruleGroup);
                }
                infoList[i] = hasVars.get(i) ? checkKeys(infoVal.toString(), analyzer) : infoVal.toString();
            }
            Debug.printLog(tag, "└详情预处理完成");

            Debug.printLog(tag, "┌获取书籍名称");
            if (!isEmpty(infoList[0])) bookInfoBean.setName(infoList[0]);
            Debug.printLog(tag, "└" + infoList[0]);

            Debug.printLog(tag, "┌获取作者名称");
            if (!isEmpty(infoList[1])) bookInfoBean.setAuthor(infoList[1]);
            Debug.printLog(tag, "└" + infoList[1]);

            Debug.printLog(tag, "┌获取分类信息");
            Debug.printLog(tag, "└" + infoList[2]);

            Debug.printLog(tag, "┌获取最新章节");
            if (!isEmpty(infoList[3])) bookShelfBean.setLastChapterName(infoList[3]);
            Debug.printLog(tag, "└" + infoList[3]);

            Debug.printLog(tag, "┌获取简介内容");
            if (!isEmpty(infoList[4])) bookInfoBean.setIntroduce(infoList[4]);
            Debug.printLog(tag, "└" + infoList[4]);

            Debug.printLog(tag, "┌获取封面网址");
            if (!isEmpty(infoList[5]))
                bookInfoBean.setCoverUrl(NetworkUtils.getAbsoluteURL(baseUrl, infoList[5]));
            Debug.printLog(tag, "└" + infoList[5]);

            Debug.printLog(tag, "┌获取目录网址");
            if (isEmpty(infoList[6])) infoList[6] = baseUrl;
            bookInfoBean.setChapterUrl(infoList[6]);
            //如果目录页和详情页相同,暂存页面内容供获取目录用
            if (infoList[6].equals(baseUrl)) bookInfoBean.setChapterListHtml(res);
            Debug.printLog(tag, "└" + bookInfoBean.getChapterUrl());
            Debug.printLog(tag, "-详情页解析完成");
        }
        else{
            StringBuilder result = new StringBuilder();
            do{ result.append(resM.group()); }while (resM.find());
            getInfosOfRegex(result.toString(), regs, ++index, bookShelfBean, analyzer);
        }
    }
    // 拆分正则表达式替换规则(如:$\d和$\d\d) /*注意:千万别用正则表达式拆分字符串,效率太低了!*/
    private static String[] splitRegexRule(String str){
        int start = 0,index = 0, len = str.length();
        List<String> arr= new ArrayList<>();
        while (start<len){
            if((str.charAt(start)=='$') && (str.charAt(start+1)>='0') && (str.charAt(start+1)<='9')){
                if(start>index) arr.add(str.substring(index, start));
                if((start+2<len) && (str.charAt(start+2)>='0') && (str.charAt(start+2)<='9')){
                    arr.add(str.substring(start, start+3));
                    index = start += 3;
                }
                else{
                    arr.add(str.substring(start, start+2));
                    index = start += 2;
                }
            }
            else{
                ++start;
            }
        }
        if(start>index) arr.add(str.substring(index, start));
        return arr.toArray(new String[arr.size()]);
    }
    // 存取字符串中的put&get参数
    private String checkKeys(String str, AnalyzeRule analyzer){
        if(str.contains("@put:{")){
            Matcher putMatcher = Pattern.compile("@put:\\{([^,]*):([^\\}]*)\\}").matcher(str);
            while (putMatcher.find()){
                str = str.replace(putMatcher.group(0), "");
                analyzer.put(putMatcher.group(1), putMatcher.group(2));
            }
        }
        if(str.contains("@get:{")){
            Matcher getMatcher = Pattern.compile("@get:\\{([^\\}]*)\\}").matcher(str);
            while (getMatcher.find()){
                str = str.replace(getMatcher.group(), analyzer.get(getMatcher.group(1)));
            }
        }
        return str;
    }
    // String数字转int数字的高效方法(利用ASCII值判断)
    private static int string2Int(String s) {
        int r = 0;
        char n;
        for (int i = 0,l=s.length(); i < l; i++) {
            n = s.charAt(i);
            if (n >= '0' && n <= '9') {
                r = r * 10 + (n - 0x30); //'0-9'的ASCII值为0x30-0x39
            }
        }
        return r;
    }
    // endregion
}

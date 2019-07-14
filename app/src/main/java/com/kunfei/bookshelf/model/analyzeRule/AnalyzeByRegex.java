package com.kunfei.bookshelf.model.analyzeRule;

import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.content.Debug;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.TextUtils.isEmpty;

public class AnalyzeByRegex {

    // 纯java模式正则表达式获取书籍详情信息
    public static void getInfosOfRegex(String res, String[] regs, int index,
                                       BookShelfBean bookShelfBean, AnalyzeRule analyzer, BookSourceBean bookSourceBean, String tag) {
        BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
        String baseUrl = bookShelfBean.getNoteUrl();
        Matcher resM = Pattern.compile(regs[index]).matcher(res);
        // 判断规则是否有效,当搜索列表规则无效时跳过详情页处理
        if (!resM.find()) {
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
            Debug.printLog(tag, "└详情预处理完成");

            Debug.printLog(tag, "┌获取书籍名称");
            if (!isEmpty(infoList.get(0)))
                bookInfoBean.setName(infoList.get(0));
            Debug.printLog(tag, "└" + infoList.get(0));

            Debug.printLog(tag, "┌获取作者名称");
            if (!isEmpty(infoList.get(1)))
                bookInfoBean.setAuthor(infoList.get(1));
            Debug.printLog(tag, "└" + infoList.get(1));

            Debug.printLog(tag, "┌获取分类信息");
            Debug.printLog(tag, "└" + infoList.get(2));

            Debug.printLog(tag, "┌获取最新章节");
            if (!isEmpty(infoList.get(3))) bookShelfBean.setLastChapterName(infoList.get(3));
            Debug.printLog(tag, "└" + infoList.get(3));

            Debug.printLog(tag, "┌获取简介内容");
            if (!isEmpty(infoList.get(4))) bookInfoBean.setIntroduce(infoList.get(4));
            Debug.printLog(tag, "└" + infoList.get(4));

            Debug.printLog(tag, "┌获取封面网址");
            if (!isEmpty(infoList.get(5)))
                bookInfoBean.setCoverUrl(infoList.get(5));
            Debug.printLog(tag, "└" + infoList.get(5));

            Debug.printLog(tag, "┌获取目录网址");
            if (isEmpty(infoList.get(6))) infoList.set(6, baseUrl);
            bookInfoBean.setChapterUrl(infoList.get(6));
            //如果目录页和详情页相同,暂存页面内容供获取目录用
            if (infoList.get(6).equals(baseUrl)) bookInfoBean.setChapterListHtml(res);
            Debug.printLog(tag, "└" + bookInfoBean.getChapterUrl());
            Debug.printLog(tag, "-详情页解析完成");
        } else {
            StringBuilder result = new StringBuilder();
            do {
                result.append(resM.group());
            } while (resM.find());
            getInfosOfRegex(result.toString(), regs, ++index, bookShelfBean, analyzer, bookSourceBean, tag);
        }
    }

    // 拆分正则表达式替换规则(如:$\d和$\d\d) /*注意:千万别用正则表达式拆分字符串,效率太低了!*/
    public static List<String> splitRegexRule(String str) {
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
        return arr;
    }

    // 存取字符串中的put&get参数
    public static String checkKeys(String str, AnalyzeRule analyzer) {
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
    public static int string2Int(String s) {
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
}

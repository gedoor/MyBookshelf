package com.kunfei.bookshelf.model.analyzeRule;

import android.os.Build;
import android.text.TextUtils;

import com.kunfei.bookshelf.bean.BookInfoBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.content.Debug;
import com.kunfei.bookshelf.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.TextUtils.isEmpty;

public class AnalyzeByRegex {

    // 纯java模式正则表达式获取书籍详情信息
    public static void getInfoOfRegex(String res, String[] regs, int index,
                                      BookShelfBean bookShelfBean, AnalyzeRule analyzer, BookSourceBean bookSourceBean, String tag) throws Exception {
        Matcher resM = Pattern.compile(regs[index]).matcher(res);
        String baseUrl = bookShelfBean.getNoteUrl();
        // 创建详情信息存储容器
        BookInfoBean bookInfoBean = bookShelfBean.getBookInfoBean();
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
            HashMap<String, String> ruleMap = new HashMap<>();
            ruleMap.put("BookName", bookSourceBean.getRuleBookName());
            ruleMap.put("BookAuthor", bookSourceBean.getRuleBookAuthor());
            ruleMap.put("BookKind", bookSourceBean.getRuleBookKind());
            ruleMap.put("LastChapter", bookSourceBean.getRuleBookLastChapter());
            ruleMap.put("Introduce", bookSourceBean.getRuleIntroduce());
            ruleMap.put("CoverUrl", bookSourceBean.getRuleCoverUrl());
            ruleMap.put("ChapterUrl", bookSourceBean.getRuleChapterUrl());
            // 分离规则参数
            List<String> ruleName = new ArrayList<>();
            List<List<String>> ruleParams = new ArrayList<>();  // 创建规则参数容器
            List<List<Integer>> ruleTypes = new ArrayList<>();  // 创建规则类型容器
            List<Boolean> hasVarParams = new ArrayList<>();     // 创建put&get标志容器
            for (String key : ruleMap.keySet()) {
                String val = ruleMap.get(key);
                ruleName.add(key);
                hasVarParams.add(!TextUtils.isEmpty(val) && (val.contains("@put") || val.contains("@get")));
                List<String> ruleParam = new ArrayList<>();
                List<Integer> ruleType = new ArrayList<>();
                AnalyzeByRegex.splitRegexRule(val, ruleParam, ruleType);
                ruleParams.add(ruleParam);
                ruleTypes.add(ruleType);
            }
            // 提取规则内容
            HashMap<String, String> ruleVal = new HashMap<>();
            StringBuilder infoVal = new StringBuilder();
            for (int i = ruleParams.size(); i-- > 0; ) {
                List<String> ruleParam = ruleParams.get(i);
                List<Integer> ruleType = ruleTypes.get(i);
                infoVal.setLength(0);
                for (int j = ruleParam.size(); j-- > 0; ) {
                    int regType = ruleType.get(j);
                    if (regType > 0) {
                        infoVal.insert(0, resM.group(regType));
                    } else if (regType < 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        infoVal.insert(0, resM.group(ruleParam.get(j)));
                    } else {
                        infoVal.insert(0, ruleParam.get(j));
                    }
                }
                ruleVal.put(ruleName.get(i), hasVarParams.get(i) ? AnalyzeByRegex.checkKeys(infoVal.toString(), analyzer) : infoVal.toString());
            }
            // 保存详情信息
            if (!isEmpty(ruleVal.get("BookName"))) bookInfoBean.setName(ruleVal.get("BookName"));
            if (!isEmpty(ruleVal.get("BookAuthor"))) bookInfoBean.setAuthor(ruleVal.get("BookAuthor"));
            if (!isEmpty(ruleVal.get("LastChapter"))) bookShelfBean.setLastChapterName(ruleVal.get("LastChapter"));
            if (!isEmpty(ruleVal.get("Introduce"))) bookInfoBean.setIntroduce(ruleVal.get("Introduce"));
            if (!isEmpty(ruleVal.get("CoverUrl"))) bookInfoBean.setCoverUrl(ruleVal.get("CoverUrl"));
            if (!isEmpty(ruleVal.get("ChapterUrl"))) bookInfoBean.setChapterUrl(NetworkUtils.getAbsoluteURL(baseUrl, ruleVal.get("ChapterUrl")));
            else bookInfoBean.setChapterUrl(baseUrl);
            //如果目录页和详情页相同,暂存页面内容供获取目录用
            if (bookInfoBean.getChapterUrl().equals(baseUrl)) bookInfoBean.setChapterListHtml(res);
            // 输出调试信息
            Debug.printLog(tag, "└详情预处理完成");
            Debug.printLog(tag, "┌获取书籍名称");
            Debug.printLog(tag, "└" + bookInfoBean.getName());
            Debug.printLog(tag, "┌获取作者名称");
            Debug.printLog(tag, "└" + bookInfoBean.getAuthor());
            Debug.printLog(tag, "┌获取最新章节");
            Debug.printLog(tag, "└" + bookShelfBean.getLastChapterName());
            Debug.printLog(tag, "┌获取简介内容");
            Debug.printLog(tag, "└" + bookInfoBean.getIntroduce());
            Debug.printLog(tag, "┌获取封面网址");
            Debug.printLog(tag, "└" + bookInfoBean.getCoverUrl());
            Debug.printLog(tag, "┌获取目录网址");
            Debug.printLog(tag, "└" + bookInfoBean.getChapterUrl());
            Debug.printLog(tag, "-详情页解析完成");
        } else {
            StringBuilder result = new StringBuilder();
            do {
                result.append(resM.group());
            } while (resM.find());
            getInfoOfRegex(result.toString(), regs, ++index, bookShelfBean, analyzer, bookSourceBean, tag);
        }
    }

    // 正则表达式解析规则数据的通用方法(暂未使用,技术储备型代码)
    public static void getInfoByRegex(String res, String[] regList, int regIndex,
                                      HashMap<String, String> ruleMap, final List<HashMap<String, String>> ruleVals) throws Exception {
        Matcher resM = Pattern.compile(regList[regIndex]).matcher(res);
        // 判断规则是否有效
        if (!resM.find()) {
            return;
        }
        // 判断索引规则是否为最后一个
        if (regIndex + 1 == regList.length) {
            // 分离规则参数
            List<String> ruleName = new ArrayList<>();
            List<List<String>> ruleParams = new ArrayList<>(); // 创建规则参数容器
            List<List<Integer>> ruleTypes = new ArrayList<>(); // 创建规则类型容器
            List<Boolean> hasVarParams = new ArrayList<>(); // 创建put&get标志容器
            for (String key : ruleMap.keySet()) {
                String val = ruleMap.get(key);
                ruleName.add(key);
                hasVarParams.add(val.contains("@put") || val.contains("@get"));
                List<String> ruleParam = new ArrayList<>();
                List<Integer> ruleType = new ArrayList<>();
                splitRegexRule(val, ruleParam, ruleType);
                ruleParams.add(ruleParam);
                ruleTypes.add(ruleType);
            }
            // 提取规则结果
            do {
                HashMap<String, String> ruleVal = new HashMap<>();
                StringBuilder infoVal = new StringBuilder();
                for (int i = ruleParams.size(); i-- > 0; ) {
                    List<String> ruleParam = ruleParams.get(i);
                    List<Integer> ruleType = ruleTypes.get(i);
                    infoVal.setLength(0);
                    for (int j = ruleParam.size(); j-- > 0; ) {
                        int regType = ruleType.get(j);
                        if (regType > 0) {
                            if (j == 0 && Objects.equals(ruleName.get(0), "ruleChapterName")) {
                                infoVal.insert(0, resM.group(regType) == null ? "" : "\uD83D\uDD12");
                            } else {
                                infoVal.insert(0, resM.group(regType));
                            }
                        } else if (regType < 0) {
                            if (j == 0 && Objects.equals(ruleName.get(0), "ruleChapterName")) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    infoVal.insert(0, resM.group(ruleParam.get(j)) == null ? "" : "\uD83D\uDD12");
                                }
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    infoVal.insert(0, resM.group(ruleParam.get(j)));
                                }
                            }
                        } else {
                            infoVal.insert(0, ruleParam.get(j));
                        }
                    }
                    ruleVal.put(ruleName.get(i), infoVal.toString());
                }
                ruleVals.add(ruleVal);
            } while (resM.find());
        } else {
            StringBuilder result = new StringBuilder();
            do {
                result.append(resM.group(0));
            } while (resM.find());
            getInfoByRegex(result.toString(), regList, ++regIndex, ruleMap, ruleVals);
        }
    }

    // 拆分正则表达式替换规则(如:$\d{1,2}或${name}) /*注意:千万别用正则表达式拆分字符串,效率太低了!*/
    public static void splitRegexRule(String str, final List<String> ruleParam, final List<Integer> ruleType) throws Exception {
        if (TextUtils.isEmpty(str)) {
            ruleParam.add("");
            ruleType.add(0);
            return;
        }
        int index = 0, start = 0, len = str.length();
        while (index < len) {
            if (str.charAt(index) == '$') {
                if (str.charAt(index + 1) == '{') {
                    if (index > start) {
                        ruleParam.add(str.substring(start, index));
                        ruleType.add(0);
                        start = index;
                    }
                    for (index += 2; index < len; index++) {
                        if (str.charAt(index) == '}') {
                            ruleParam.add(str.substring(start + 2, index));
                            ruleType.add(-1);
                            start = ++index;
                            break;
                        } else if (str.charAt(index) == '$' || str.charAt(index) == '@') {
                            break;
                        }
                    }
                } else if ((str.charAt(index + 1) >= '0') && (str.charAt(index + 1) <= '9')) {
                    if (index > start) {
                        ruleParam.add(str.substring(start, index));
                        ruleType.add(0);
                        start = index;
                    }
                    if ((index + 2 < len) && (str.charAt(index + 2) >= '0') && (str.charAt(index + 2) <= '9')) {
                        ruleParam.add(str.substring(start, index + 3));
                        ruleType.add(string2Int(ruleParam.get(ruleParam.size() - 1)));
                        start = index += 3;
                    } else {
                        ruleParam.add(str.substring(start, index + 2));
                        ruleType.add(string2Int(ruleParam.get(ruleParam.size() - 1)));
                        start = index += 2;
                    }
                } else {
                    ++index;
                }
            } else {
                ++index;
            }
        }
        if (index > start) {
            ruleParam.add(str.substring(start, index));
            ruleType.add(0);
        }
    }

    // 存取字符串中的put&get参数
    public static String checkKeys(String str, AnalyzeRule analyzer) throws Exception {
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

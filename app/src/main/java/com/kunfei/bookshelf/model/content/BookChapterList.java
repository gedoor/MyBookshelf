package com.kunfei.bookshelf.model.content;

import android.os.Build;
import android.text.TextUtils;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseModelImpl;
import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.bean.WebChapterBean;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeByRegex;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeRule;
import com.kunfei.bookshelf.model.analyzeRule.AnalyzeUrl;
import com.kunfei.bookshelf.model.task.AnalyzeNextUrlTask;

import org.jsoup.nodes.Element;
import org.mozilla.javascript.NativeObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import retrofit2.Response;

public class BookChapterList {
    private String tag;
    private BookSourceBean bookSourceBean;
    private List<WebChapterBean> webChapterBeans;
    private boolean dx = false;
    private boolean analyzeNextUrl;
    private CompositeDisposable compositeDisposable;
    private String chapterListUrl;

    BookChapterList(String tag, BookSourceBean bookSourceBean, boolean analyzeNextUrl) {
        this.tag = tag;
        this.bookSourceBean = bookSourceBean;
        this.analyzeNextUrl = analyzeNextUrl;
    }

    public Observable<List<BookChapterBean>> analyzeChapterList(final String s, final BookShelfBean bookShelfBean, Map<String, String> headerMap) {
        return Observable.create(e -> {
            if (TextUtils.isEmpty(s)) {
                e.onError(new Throwable(MApplication.getInstance().getString(R.string.get_chapter_list_error) + bookShelfBean.getBookInfoBean().getChapterUrl()));
                return;
            } else {
                Debug.printLog(tag, 1, "┌成功获取目录页", analyzeNextUrl);
                Debug.printLog(tag, 1, "└" + bookShelfBean.getBookInfoBean().getChapterUrl(), analyzeNextUrl);
            }
            bookShelfBean.setTag(tag);
            AnalyzeRule analyzer = new AnalyzeRule(bookShelfBean);
            String ruleChapterList = bookSourceBean.getRuleChapterList();
            if (ruleChapterList != null && ruleChapterList.startsWith("-")) {
                dx = true;
                ruleChapterList = ruleChapterList.substring(1);
            }
            chapterListUrl = bookShelfBean.getBookInfoBean().getChapterUrl();
            WebChapterBean webChapterBean = analyzeChapterList(s, chapterListUrl, ruleChapterList, analyzeNextUrl, analyzer, dx);
            final List<BookChapterBean> chapterList = webChapterBean.getData();

            final List<String> chapterUrlS = new ArrayList<>(webChapterBean.getNextUrlList());
            if (chapterUrlS.isEmpty() || !analyzeNextUrl) {
                finish(chapterList, e);
            }
            //下一页为单页
            else if (chapterUrlS.size() == 1) {
                List<String> usedUrl = new ArrayList<>();
                usedUrl.add(bookShelfBean.getBookInfoBean().getChapterUrl());
                //循环获取直到下一页为空
                Debug.printLog(tag, "正在加载下一页");
                while (!chapterUrlS.isEmpty() && !usedUrl.contains(chapterUrlS.get(0))) {
                    usedUrl.add(chapterUrlS.get(0));
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(chapterUrlS.get(0), headerMap, tag);
                    try {
                        String body;
                        Response<String> response = BaseModelImpl.getInstance().getResponseO(analyzeUrl)
                                .blockingFirst();
                        body = response.body();
                        webChapterBean = analyzeChapterList(body, chapterUrlS.get(0), ruleChapterList, false, analyzer, dx);
                        chapterList.addAll(webChapterBean.getData());
                        chapterUrlS.clear();
                        chapterUrlS.addAll(webChapterBean.getNextUrlList());
                    } catch (Exception exception) {
                        if (!e.isDisposed()) {
                            e.onError(exception);
                        }
                    }
                }
                Debug.printLog(tag, "下一页加载完成共" + usedUrl.size() + "页");
                finish(chapterList, e);
            }
            //下一页为多页
            else {
                Debug.printLog(tag, "正在加载其它" + chapterUrlS.size() + "页");
                compositeDisposable = new CompositeDisposable();
                webChapterBeans = new ArrayList<>();
                AnalyzeNextUrlTask.Callback callback = new AnalyzeNextUrlTask.Callback() {
                    @Override
                    public void addDisposable(Disposable disposable) {
                        compositeDisposable.add(disposable);
                    }

                    @Override
                    public void analyzeFinish(WebChapterBean bean, List<BookChapterBean> chapterListBeans) {
                        if (nextUrlFinish(bean, chapterListBeans)) {
                            for (WebChapterBean chapterBean : webChapterBeans) {
                                chapterList.addAll(chapterBean.getData());
                            }
                            Debug.printLog(tag, "其它页加载完成,目录共" + chapterList.size() + "条");
                            finish(chapterList, e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        compositeDisposable.dispose();
                        e.onError(throwable);
                    }
                };
                for (String url : chapterUrlS) {
                    final WebChapterBean bean = new WebChapterBean(url);
                    webChapterBeans.add(bean);
                }
                for (WebChapterBean bean : webChapterBeans) {
                    BookChapterList bookChapterList = new BookChapterList(tag, bookSourceBean, false);
                    AnalyzeUrl analyzeUrl = new AnalyzeUrl(bean.getUrl(), headerMap, tag);
                    new AnalyzeNextUrlTask(bookChapterList, bean, bookShelfBean, headerMap)
                            .setCallback(callback)
                            .analyzeUrl(analyzeUrl);
                }
            }
        });
    }

    private synchronized boolean nextUrlFinish(WebChapterBean webChapterBean, List<BookChapterBean> bookChapterBeans) {
        webChapterBean.setData(bookChapterBeans);
        for (WebChapterBean bean : webChapterBeans) {
            if (bean.noData()) return false;
        }
        return true;
    }

    private void finish(List<BookChapterBean> chapterList, Emitter<List<BookChapterBean>> emitter) {
        //去除重复,保留后面的,先倒序,从后面往前判断
        if (!dx) {
            Collections.reverse(chapterList);
        }
        LinkedHashSet<BookChapterBean> lh = new LinkedHashSet<>(chapterList);
        chapterList = new ArrayList<>(lh);
        Collections.reverse(chapterList);
        Debug.printLog(tag, 1, "-目录解析完成", analyzeNextUrl);
        emitter.onNext(chapterList);
        emitter.onComplete();
    }

    private WebChapterBean analyzeChapterList(String s, String chapterUrl, String ruleChapterList,
                                              boolean printLog, AnalyzeRule analyzer, boolean dx) throws Exception {
        List<String> nextUrlList = new ArrayList<>();
        analyzer.setContent(s, chapterUrl);
        if (!TextUtils.isEmpty(bookSourceBean.getRuleChapterUrlNext()) && analyzeNextUrl) {
            Debug.printLog(tag, 1, "┌获取目录下一页网址", printLog);
            nextUrlList = analyzer.getStringList(bookSourceBean.getRuleChapterUrlNext(), true);
            int thisUrlIndex = nextUrlList.indexOf(chapterUrl);
            if (thisUrlIndex != -1) {
                nextUrlList.remove(thisUrlIndex);
            }
            Debug.printLog(tag, 1, "└" + nextUrlList.toString(), printLog);
        }

        List<BookChapterBean> chapterBeans = new ArrayList<>();
        Debug.printLog(tag, 1, "┌解析目录列表", printLog);
        // 仅使用java正则表达式提取目录列表
        if (ruleChapterList.startsWith(":")) {
            ruleChapterList = ruleChapterList.substring(1);
            regexChapter(s, ruleChapterList.split("&&"), 0, analyzer, chapterBeans);
            if (chapterBeans.size() == 0) {
                Debug.printLog(tag, 1, "└找到 0 个章节", printLog);
                return new WebChapterBean(chapterBeans, new LinkedHashSet<>(nextUrlList));
            }
        }
        // 使用AllInOne规则模式提取目录列表
        else if (ruleChapterList.startsWith("+")) {
            ruleChapterList = ruleChapterList.substring(1);
            List<Object> collections = analyzer.getElements(ruleChapterList);
            if (collections.size() == 0) {
                Debug.printLog(tag, 1, "└找到 0 个章节", printLog);
                return new WebChapterBean(chapterBeans, new LinkedHashSet<>(nextUrlList));
            }
            String nameRule = bookSourceBean.getRuleChapterName();
            String linkRule = bookSourceBean.getRuleContentUrl();
            String name = "";
            String link = "";
            for (Object object : collections) {
                if (object instanceof NativeObject) {
                    name = String.valueOf(((NativeObject) object).get(nameRule));
                    link = String.valueOf(((NativeObject) object).get(linkRule));
                } else if (object instanceof Element) {
                    name = ((Element) object).text();
                    link = ((Element) object).attr(linkRule);
                }
                addChapter(chapterBeans, name, link);
            }
        }
        // 使用默认规则解析流程提取目录列表
        else {
            List<Object> collections = analyzer.getElements(ruleChapterList);
            if (collections.size() == 0) {
                Debug.printLog(tag, 1, "└找到 0 个章节", printLog);
                return new WebChapterBean(chapterBeans, new LinkedHashSet<>(nextUrlList));
            }
            List<AnalyzeRule.SourceRule> nameRule = analyzer.splitSourceRule(bookSourceBean.getRuleChapterName());
            List<AnalyzeRule.SourceRule> linkRule = analyzer.splitSourceRule(bookSourceBean.getRuleContentUrl());
            for (Object object : collections) {
                analyzer.setContent(object, chapterUrl);
                addChapter(chapterBeans, analyzer.getString(nameRule), analyzer.getString(linkRule));
            }
        }
        Debug.printLog(tag, 1, "└找到 " + chapterBeans.size() + " 个章节", printLog);
        BookChapterBean firstChapter;
        if (dx) {
            Debug.printLog(tag, 1, "-倒序", printLog);
            firstChapter = chapterBeans.get(chapterBeans.size() - 1);
        } else {
            firstChapter = chapterBeans.get(0);
        }
        Debug.printLog(tag, 1, "┌获取章节名称", printLog);
        Debug.printLog(tag, 1, "└" + firstChapter.getDurChapterName(), printLog);
        Debug.printLog(tag, 1, "┌获取章节网址", printLog);
        Debug.printLog(tag, 1, "└" + firstChapter.getDurChapterUrl(), printLog);
        return new WebChapterBean(chapterBeans, new LinkedHashSet<>(nextUrlList));
    }

    private void addChapter(final List<BookChapterBean> chapterBeans, String name, String link) {
        if (TextUtils.isEmpty(name)) return;
        if (TextUtils.isEmpty(link)) link = chapterListUrl;
        chapterBeans.add(new BookChapterBean(tag, name, link));
    }

    // region 纯java模式正则表达式获取目录列表
    private void regexChapter(String str, String[] regex, int index, AnalyzeRule analyzer, final List<BookChapterBean> chapterBeans) throws Exception {
        Matcher resM = Pattern.compile(regex[index]).matcher(str);
        if (!resM.find()) {
            return;
        }
        if (index + 1 == regex.length) {
            // 获取解析规则
            String nameRule = bookSourceBean.getRuleChapterName();
            String linkRule = bookSourceBean.getRuleContentUrl();
            if (TextUtils.isEmpty(nameRule) || TextUtils.isEmpty(linkRule)) return;
            // 替换@get规则
            nameRule = analyzer.replaceGet(bookSourceBean.getRuleChapterName());
            linkRule = analyzer.replaceGet(bookSourceBean.getRuleContentUrl());
            // 分离规则参数
            List<String> nameParams = new ArrayList<>();
            List<Integer> nameGroups = new ArrayList<>();
            AnalyzeByRegex.splitRegexRule(nameRule, nameParams, nameGroups);
            List<String> linkParams = new ArrayList<>();
            List<Integer> linkGroups = new ArrayList<>();
            AnalyzeByRegex.splitRegexRule(linkRule, linkParams, linkGroups);
            // 是否包含VIP规则(hasVipRule>1 时视为包含vip规则)
            int hasVipRule = 0;
            for (int i = nameGroups.size(); i-- > 0; ) {
                if (nameGroups.get(i) != 0) {
                    ++hasVipRule;
                }
            }
            String vipNameGroup = "";
            int vipNumGroup = 0;
            if ((nameGroups.get(0) != 0) && (hasVipRule > 1)) {
                vipNumGroup = nameGroups.remove(0);
                vipNameGroup = nameParams.remove(0);
            }
            // 创建结果缓存
            StringBuilder cName = new StringBuilder();
            StringBuilder cLink = new StringBuilder();
            // 提取书籍目录
            if (vipNumGroup != 0) {
                do {
                    cName.setLength(0);
                    cLink.setLength(0);
                    for (int i = nameParams.size(); i-- > 0; ) {
                        if (nameGroups.get(i) > 0) {
                            cName.insert(0, resM.group(nameGroups.get(i)));
                        } else if (nameGroups.get(i) < 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            cName.insert(0, resM.group(nameParams.get(i)));
                        } else {
                            cName.insert(0, nameParams.get(i));
                        }
                    }
                    if (vipNumGroup > 0) {
                        cName.insert(0, resM.group(vipNumGroup) == null ? "" : "\uD83D\uDD12");
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        cName.insert(0, resM.group(vipNameGroup) == null ? "" : "\uD83D\uDD12");
                    } else {
                        cName.insert(0, vipNameGroup);
                    }

                    for (int i = linkParams.size(); i-- > 0; ) {
                        if (linkGroups.get(i) > 0) {
                            cLink.insert(0, resM.group(linkGroups.get(i)));
                        } else if (linkGroups.get(i) < 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            cLink.insert(0, resM.group(linkParams.get(i)));
                        } else {
                            cLink.insert(0, linkParams.get(i));
                        }
                    }

                    addChapter(chapterBeans, cName.toString(), cLink.toString());
                } while (resM.find());
            } else {
                do {
                    cName.setLength(0);
                    cLink.setLength(0);
                    for (int i = nameParams.size(); i-- > 0; ) {
                        if (nameGroups.get(i) > 0) {
                            cName.insert(0, resM.group(nameGroups.get(i)));
                        } else if (nameGroups.get(i) < 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            cName.insert(0, resM.group(nameParams.get(i)));
                        } else {
                            cName.insert(0, nameParams.get(i));
                        }
                    }

                    for (int i = linkParams.size(); i-- > 0; ) {
                        if (linkGroups.get(i) > 0) {
                            cLink.insert(0, resM.group(linkGroups.get(i)));
                        } else if (linkGroups.get(i) < 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            cLink.insert(0, resM.group(linkParams.get(i)));
                        } else {
                            cLink.insert(0, linkParams.get(i));
                        }
                    }

                    addChapter(chapterBeans, cName.toString(), cLink.toString());
                } while (resM.find());
            }
        } else {
            StringBuilder result = new StringBuilder();
            do {
                result.append(resM.group(0));
            } while (resM.find());
            regexChapter(result.toString(), regex, ++index, analyzer, chapterBeans);
        }
    }
    // endregion
}
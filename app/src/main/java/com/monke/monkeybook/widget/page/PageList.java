package com.monke.monkeybook.widget.page;

import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.StaticLayout;

import com.monke.monkeybook.bean.ChapterListBean;
import com.monke.monkeybook.help.ChapterContentHelp;
import com.monke.monkeybook.utils.NetworkUtil;
import com.monke.monkeybook.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

class PageList {
    private PageLoader pageLoader;
    private ChapterContentHelp contentHelper = new ChapterContentHelp();

    PageList(PageLoader pageLoader) {
        this.pageLoader = pageLoader;
    }

    TxtChapter dealLoadPageList(ChapterListBean chapter, boolean isPrepare) {
        TxtChapter txtChapter = new TxtChapter(chapter.getDurChapterIndex());
        // 判断章节是否存在
        if (!isPrepare || pageLoader.noChapterData(chapter)) {
            if (pageLoader instanceof PageLoaderNet && !NetworkUtil.isNetWorkAvailable()) {
                txtChapter.setStatus(Enum.PageStatus.ERROR);
                txtChapter.setMsg("网络连接不可用");
            }
            return txtChapter;
        }
        String content;
        try {
            content = pageLoader.getChapterContent(chapter);
        } catch (Exception e) {
            txtChapter.setStatus(Enum.PageStatus.ERROR);
            txtChapter.setMsg("读取内容出错\n" + e.getLocalizedMessage());
            return txtChapter;
        }
        if (content == null) {
            txtChapter.setStatus(Enum.PageStatus.ERROR);
            txtChapter.setMsg("缓存文件不存在");
            return txtChapter;
        }
        return loadPageList(chapter, content);
    }

    /**
     * 将章节数据，解析成页面列表
     *
     * @param chapter：章节信息
     * @param content：章节的文本
     */
    private TxtChapter loadPageList(ChapterListBean chapter, @NonNull String content) {
        //生成的页面
        TxtChapter txtChapter = new TxtChapter(chapter.getDurChapterIndex());
        content = contentHelper.replaceContent(pageLoader.bookShelfBean.getBookInfoBean().getName(), pageLoader.bookShelfBean.getTag(), content);
        String allLine[] = content.split("\n");
        List<String> lines = new ArrayList<>();
        int rHeight = pageLoader.mVisibleHeight - pageLoader.contentMarginHeight * 2;
        int titleLinesCount = 0;
        boolean showTitle = pageLoader.readBookControl.getShowTitle(); // 是否展示标题
        String paragraph = null;
        if (showTitle) {
            paragraph = contentHelper.replaceContent(pageLoader.bookShelfBean.getBookInfoBean().getName(), pageLoader.bookShelfBean.getTag(), chapter.getDurChapterName());
            paragraph = paragraph.trim() + "\n";
        }
        int i = 1;
        while (showTitle || i < allLine.length) {
            // 重置段落
            if (!showTitle) {
                paragraph = allLine[i].replaceAll("\\s", " ").trim();
                i++;
                if (paragraph.equals("")) continue;
                paragraph = StringUtils.halfToFull("  ") + paragraph + "\n";
            }
            int wordCount;
            String subStr;
            while (paragraph.length() > 0) {
                //当前空间，是否容得下一行文字
                if (showTitle) {
                    rHeight -= pageLoader.mTitlePaint.getTextSize();
                } else {
                    rHeight -= pageLoader.mTextPaint.getTextSize();
                }
                // 一页已经填充满了，创建 TextPage
                if (rHeight <= 0) {
                    // 创建Page
                    TxtPage page = new TxtPage();
                    page.position = txtChapter.getTxtPageList().size();
                    page.title = chapter.getDurChapterName();
                    page.lines = new ArrayList<>(lines);
                    page.titleLines = titleLinesCount;
                    txtChapter.addPage(page);
                    addTxtPageLengthList(txtChapter, page.getContent().length());
                    // 重置Lines
                    lines.clear();
                    rHeight = pageLoader.mVisibleHeight - pageLoader.contentMarginHeight * 2;
                    titleLinesCount = 0;

                    continue;
                }

                //测量一行占用的字节数
                if (showTitle) {
                    Layout tempLayout = new StaticLayout(paragraph, pageLoader.mTitlePaint, pageLoader.mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                    wordCount = tempLayout.getLineEnd(0);
                } else {
                    Layout tempLayout = new StaticLayout(paragraph, pageLoader.mTextPaint, pageLoader.mVisibleWidth, Layout.Alignment.ALIGN_NORMAL, 0, 0, false);
                    wordCount = tempLayout.getLineEnd(0);

                }

                subStr = paragraph.substring(0, wordCount);
                if (!subStr.equals("\n")) {
                    //将一行字节，存储到lines中
                    lines.add(subStr);

                    //设置段落间距
                    if (showTitle) {
                        titleLinesCount += 1;
                        rHeight -= pageLoader.mTitleInterval;
                    } else {
                        rHeight -= pageLoader.mTextInterval;
                    }
                }
                //裁剪
                paragraph = paragraph.substring(wordCount);
            }

            //增加段落的间距
            if (!showTitle && lines.size() != 0) {
                rHeight = rHeight - pageLoader.mTextPara + pageLoader.mTextInterval;
            }

            if (showTitle) {
                rHeight = rHeight - pageLoader.mTitlePara + pageLoader.mTitleInterval;
                showTitle = false;
            }
        }

        if (lines.size() != 0) {
            //创建Page
            TxtPage page = new TxtPage();
            page.position = txtChapter.getTxtPageList().size();
            page.title = chapter.getDurChapterName();
            page.lines = new ArrayList<>(lines);
            page.titleLines = titleLinesCount;
            txtChapter.addPage(page);
            addTxtPageLengthList(txtChapter, page.getContent().length());
            //重置Lines
            lines.clear();
        }
        txtChapter.setStatus(Enum.PageStatus.FINISH);
        return txtChapter;
    }

    private void addTxtPageLengthList(TxtChapter txtChapter, int length) {
        if (txtChapter.getTxtPageLengthList().isEmpty()) {
            txtChapter.getTxtPageLengthList().add(length);
        } else {
            txtChapter.getTxtPageLengthList().add(txtChapter.getTxtPageLengthList().get(txtChapter.getTxtPageLengthList().size() - 1) + length);
        }
    }
}

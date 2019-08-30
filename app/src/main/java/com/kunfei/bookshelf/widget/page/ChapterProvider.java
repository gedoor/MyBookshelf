package com.kunfei.bookshelf.widget.page;

import android.text.Layout;
import android.text.StaticLayout;

import androidx.annotation.NonNull;

import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.help.ChapterContentHelp;
import com.kunfei.bookshelf.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

class ChapterProvider {
    private PageLoader pageLoader;
    private ChapterContentHelp contentHelper = new ChapterContentHelp();

    ChapterProvider(PageLoader pageLoader) {
        this.pageLoader = pageLoader;
    }

    TxtChapter dealLoadPageList(BookChapterBean chapter, boolean isPrepare) {
        TxtChapter txtChapter = new TxtChapter(chapter.getDurChapterIndex());
        // 判断章节是否存在
        if (!isPrepare || pageLoader.noChapterData(chapter)) {
            if (pageLoader instanceof PageLoaderNet && !NetworkUtils.isNetWorkAvailable()) {
                txtChapter.setStatus(TxtChapter.Status.ERROR);
                txtChapter.setMsg("网络连接不可用");
            }
            return txtChapter;
        }
        String content;
        try {
            content = pageLoader.getChapterContent(chapter);
        } catch (Exception e) {
            txtChapter.setStatus(TxtChapter.Status.ERROR);
            txtChapter.setMsg("读取内容出错\n" + e.getLocalizedMessage());
            return txtChapter;
        }
        if (content == null) {
            txtChapter.setStatus(TxtChapter.Status.ERROR);
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
    private TxtChapter loadPageList(BookChapterBean chapter, @NonNull String content) {
        //生成的页面
        TxtChapter txtChapter = new TxtChapter(chapter.getDurChapterIndex());
        if (pageLoader.book.isAudio()) {
            txtChapter.setStatus(TxtChapter.Status.FINISH);
            txtChapter.setMsg(content);
            TxtPage page = new TxtPage(txtChapter.getTxtPageList().size());
            page.setTitle(chapter.getDurChapterName());
            page.addLine(chapter.getDurChapterName());
            page.addLine(content);
            page.setTitleLines(1);
            txtChapter.addPage(page);
            addTxtPageLength(txtChapter, page.getContent().length());
            txtChapter.addPage(page);
            return txtChapter;
        }
        content = contentHelper.replaceContent(pageLoader.book.getBookInfoBean().getName(), pageLoader.book.getTag(), content, pageLoader.book.getReplaceEnable());
        String[] allLine = content.split("\n");
        List<String> lines = new ArrayList<>();
        List<TxtLine> txtLists = new ArrayList<>();//记录每个字的位置 //pzl
        int rHeight = pageLoader.mVisibleHeight - pageLoader.contentMarginHeight * 2;
        int titleLinesCount = 0;
        boolean showTitle = pageLoader.readBookControl.getShowTitle(); // 是否展示标题
        String paragraph = null;
        if (showTitle) {
            paragraph = contentHelper.replaceContent(pageLoader.book.getBookInfoBean().getName(), pageLoader.book.getTag(), chapter.getDurChapterName(), pageLoader.book.getReplaceEnable());
            paragraph = paragraph.trim() + "\n";
        }
        int i = 1;
        while (showTitle || i < allLine.length) {
            // 重置段落
            if (!showTitle) {
                paragraph = allLine[i].replaceAll("\\s", " ").trim();
                i++;
                if (paragraph.equals("")) continue;
                paragraph = pageLoader.indent + paragraph + "\n";
            }
            addParagraphLength(txtChapter, paragraph.length());
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
                    TxtPage page = new TxtPage(txtChapter.getTxtPageList().size());
                    page.setTitle(chapter.getDurChapterName());
                    page.addLines(lines);
                    page.setTxtLists(new ArrayList<>(txtLists));
                    page.setTitleLines(titleLinesCount);
                    txtChapter.addPage(page);
                    addTxtPageLength(txtChapter, page.getContent().length());
                    // 重置Lines
                    lines.clear();
                    txtLists.clear();//pzl
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
                    //begin pzl
                    //记录每个字的位置
                    char[] cs = subStr.toCharArray();
                    TxtLine txtList = new TxtLine();//每一行
                    txtList.setCharsData(new ArrayList<TxtChar>());
                    for (char c : cs) {
                        String mesasrustr = String.valueOf(c);
                        float charwidth = pageLoader.mTextPaint.measureText(mesasrustr);
                        if (showTitle) {
                            charwidth = pageLoader.mTitlePaint.measureText(mesasrustr);
                        }
                        TxtChar txtChar = new TxtChar();
                        txtChar.setChardata(c);
                        txtChar.setCharWidth(charwidth);//字宽
                        txtChar.setIndex(66);//每页每个字的位置
                        txtList.getCharsData().add(txtChar);
                    }
                    txtLists.add(txtList);
                    //end pzl
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
            TxtPage page = new TxtPage(txtChapter.getTxtPageList().size());
            page.setTitle(chapter.getDurChapterName());
            page.addLines(lines);
            page.setTxtLists(new ArrayList<>(txtLists));
            page.setTitleLines(titleLinesCount);
            txtChapter.addPage(page);
            addTxtPageLength(txtChapter, page.getContent().length());
            //重置Lines
            lines.clear();
            txtLists.clear();
        }
        if (txtChapter.getPageSize() > 0) {
            txtChapter.setStatus(TxtChapter.Status.FINISH);
        } else {
            txtChapter.setStatus(TxtChapter.Status.ERROR);
            txtChapter.setMsg("未加载到内容");
        }
        return txtChapter;
    }

    private void addTxtPageLength(TxtChapter txtChapter, int length) {
        if (txtChapter.getTxtPageLengthList().isEmpty()) {
            txtChapter.addTxtPageLength(length);
        } else {
            txtChapter.addTxtPageLength(txtChapter.getTxtPageLengthList().get(txtChapter.getTxtPageLengthList().size() - 1) + length);
        }
    }

    private void addParagraphLength(TxtChapter txtChapter, int length) {
        if (txtChapter.getParagraphLengthList().isEmpty()) {
            txtChapter.addParagraphLength(length);
        } else {
            txtChapter.addParagraphLength(txtChapter.getParagraphLengthList().get(txtChapter.getParagraphLengthList().size() - 1) + length);
        }
    }
}

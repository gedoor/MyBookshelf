package com.kunfei.bookshelf.widget.page;

import android.text.TextUtils;

import com.kunfei.bookshelf.bean.BookChapterBean;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.model.TxtChapterRuleManager;
import com.kunfei.bookshelf.utils.EncodingDetect;
import com.kunfei.bookshelf.utils.IOUtils;
import com.kunfei.bookshelf.utils.MD5Utils;
import com.kunfei.bookshelf.utils.RxUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;

import static com.kunfei.bookshelf.help.FileHelp.BLANK;

/**
 * 加载本地书籍
 */
public class PageLoaderText extends PageLoader {
    private static final String TAG = "PageLoaderText";
    //默认从文件中获取数据的长度
    private final static int BUFFER_SIZE = 512 * 1024;
    //没有标题的时候，每个章节的最大长度
    private final static int MAX_LENGTH_WITH_NO_CHAPTER = 10 * 1024;

    private List<String> chapterPatterns = new ArrayList<>();
    //章节解析模式
    private Pattern mChapterPattern = null;
    //获取书本的文件
    private File mBookFile;
    //编码类型
    private Charset mCharset;

    PageLoaderText(PageView pageView, BookShelfBean bookShelfBean, Callback callback) {
        super(pageView, bookShelfBean, callback);
    }

    @Override
    public void refreshChapterList() {
        Single.create((SingleOnSubscribe<List<BookChapterBean>>) e -> {
            // 对于文件是否存在，或者为空的判断，不作处理。 ==> 在文件打开前处理过了。
            mBookFile = new File(book.getNoteUrl());
            //获取文件编码
            if (TextUtils.isEmpty(book.getBookInfoBean().getCharset())) {
                book.getBookInfoBean().setCharset(EncodingDetect.getJavaEncode(mBookFile));
            }
            mCharset = Charset.forName(book.getBookInfoBean().getCharset());

            Long lastModified = mBookFile.lastModified();
            if (book.getFinalRefreshData() < lastModified) {
                book.setFinalRefreshData(lastModified);
                book.setHasUpdate(true);
            }
            if (book.getHasUpdate() || callback.getChapterList().size() == 0) {
                List<BookChapterBean> chapterBeanList = loadChapters();
                book.setHasUpdate(false);
                e.onSuccess(chapterBeanList);
            } else {
                e.onSuccess(new ArrayList<>());
            }
        }).compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<List<BookChapterBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(List<BookChapterBean> bookChapterBeans) {
                        isChapterListPrepare = true;
                        // 目录加载完成，执行回调操作。
                        if (!bookChapterBeans.isEmpty()) {
                            callback.onCategoryFinish(bookChapterBeans);
                        }
                        // 打开章节
                        skipToChapter(book.getDurChapter(), book.getDurChapterPage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        durDhapterError(e.getMessage());
                    }
                });
    }

    @Override
    protected String getChapterContent(BookChapterBean chapter) {
        //从文件中获取数据
        byte[] content = getChapterContentByte(chapter);
        return new String(content, mCharset);
    }

    @Override
    protected boolean noChapterData(BookChapterBean chapter) {
        return false;
    }

    @Override
    public void updateChapter() {
        mPageView.getActivity().toast("目录更新中");
        Single.create((SingleOnSubscribe<List<BookChapterBean>>) e -> {
            BookshelfHelp.delChapterList(book.getNoteUrl());
            //获取文件编码
            if (TextUtils.isEmpty(book.getBookInfoBean().getCharset())) {
                book.getBookInfoBean().setCharset(EncodingDetect.getJavaEncode(mBookFile));
            }
            mCharset = Charset.forName(book.getBookInfoBean().getCharset());
            e.onSuccess(loadChapters());
        })
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new SingleObserver<List<BookChapterBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(List<BookChapterBean> value) {
                        isChapterListPrepare = true;
                        mPageView.getActivity().toast("更新完成");
                        book.setHasUpdate(false);

                        // 提示目录加载完成
                        if (callback != null) {
                            callback.onCategoryFinish(value);
                        }

                        // 加载并显示当前章节
                        openChapter(book.getDurChapterPage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        durDhapterError(e.getMessage());
                    }
                });
    }

    /**
     * 从文件中提取一章的内容
     */
    private byte[] getChapterContentByte(BookChapterBean chapter) {
        RandomAccessFile bookStream = null;
        try {
            bookStream = new RandomAccessFile(mBookFile, "r");
            bookStream.seek(chapter.getStart());
            int extent = (int) (chapter.getEnd() - chapter.getStart());
            byte[] content = new byte[extent];
            bookStream.read(content, 0, extent);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(bookStream);
        }
        return new byte[0];
    }

    /**
     * 1. 检查文件中是否存在章节名
     * 2. 判断文件中使用的章节名类型的正则表达式
     *
     * @return 是否存在章节名
     */
    private boolean checkChapterType(RandomAccessFile bookStream) throws IOException {
        chapterPatterns.clear();
        if (TextUtils.isEmpty(book.getBookInfoBean().getChapterUrl())) {
            chapterPatterns.addAll(TxtChapterRuleManager.enabledRuleList());
        } else {
            chapterPatterns.add(book.getBookInfoBean().getChapterUrl());
        }
        //首先获取128k的数据
        byte[] buffer = new byte[BUFFER_SIZE / 4];
        int length = bookStream.read(buffer, 0, buffer.length);
        //进行章节匹配
        for (String str : chapterPatterns) {
            Pattern pattern = Pattern.compile(str, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(new String(buffer, 0, length, mCharset));
            //如果匹配存在，那么就表示当前章节使用这种匹配方式
            if (matcher.find()) {
                mChapterPattern = pattern;
                //重置指针位置
                bookStream.seek(0);
                return true;
            }
        }

        //重置指针位置
        bookStream.seek(0);
        return false;
    }

    /**
     * 未完成的部分:
     * 1. 序章的添加
     * 2. 章节存在的书本的虚拟分章效果
     */
    private List<BookChapterBean> loadChapters() throws IOException {
        List<BookChapterBean> mChapterList = new ArrayList<>();
        //获取文件流
        RandomAccessFile bookStream = new RandomAccessFile(mBookFile, "r");
        //寻找匹配文章标题的正则表达式，判断是否存在章节名
        boolean hasChapter = checkChapterType(bookStream);
        //加载章节
        byte[] buffer = new byte[BUFFER_SIZE];
        //获取到的块起始点，在文件中的位置
        long curOffset = 0;
        //block的个数
        int blockPos = 0;
        //读取的长度
        int length;
        int allLength = 0;

        //获取文件中的数据到buffer，直到没有数据为止
        while ((length = bookStream.read(buffer, 0, buffer.length)) > 0) {
            ++blockPos;
            //如果存在Chapter
            if (hasChapter) {
                //将数据转换成String
                String blockContent = new String(buffer, 0, length, mCharset);
                int lastN = blockContent.lastIndexOf("\n");
                if (lastN != 0) {
                    blockContent = blockContent.substring(0, lastN);
                    length = blockContent.getBytes(mCharset).length;
                    allLength = allLength + length;
                    bookStream.seek(allLength);
                }
                //当前Block下使过的String的指针
                int seekPos = 0;
                //进行正则匹配
                Matcher matcher = mChapterPattern.matcher(blockContent);
                //如果存在相应章节
                while (matcher.find()) {
                    //获取匹配到的字符在字符串中的起始位置
                    int chapterStart = matcher.start();

                    //如果 seekPos == 0 && nextChapterPos != 0 表示当前block处前面有一段内容
                    //第一种情况一定是序章 第二种情况可能是上一个章节的内容
                    if (seekPos == 0 && chapterStart != 0) {
                        //获取当前章节的内容
                        String chapterContent = blockContent.substring(seekPos, chapterStart);
                        //设置指针偏移
                        seekPos += chapterContent.length();

                        if (mChapterList.size() == 0) { //如果当前没有章节，那么就是序章
                            //加入简介
                            book.getBookInfoBean().setIntroduce(chapterContent);

                            //创建当前章节
                            BookChapterBean curChapter = new BookChapterBean();
                            curChapter.setDurChapterName(matcher.group());
                            curChapter.setStart((long) chapterContent.getBytes(mCharset).length);
                            mChapterList.add(curChapter);
                        } else {  //否则就block分割之后，上一个章节的剩余内容
                            //获取上一章节
                            BookChapterBean lastChapter = mChapterList.get(mChapterList.size() - 1);
                            //将当前段落添加上一章去
                            lastChapter.setEnd(lastChapter.getEnd() + chapterContent.getBytes(mCharset).length);

                            //创建当前章节
                            BookChapterBean curChapter = new BookChapterBean();
                            curChapter.setDurChapterName(matcher.group());
                            curChapter.setStart(lastChapter.getEnd());
                            mChapterList.add(curChapter);
                        }
                    } else {
                        //是否存在章节
                        if (mChapterList.size() != 0) {
                            //获取章节内容
                            String chapterContent = blockContent.substring(seekPos, matcher.start());
                            seekPos += chapterContent.length();

                            //获取上一章节
                            BookChapterBean lastChapter = mChapterList.get(mChapterList.size() - 1);
                            lastChapter.setEnd(lastChapter.getStart() + chapterContent.getBytes(mCharset).length);

                            //创建当前章节
                            BookChapterBean curChapter = new BookChapterBean();
                            curChapter.setDurChapterName(matcher.group());
                            curChapter.setStart(lastChapter.getEnd());
                            mChapterList.add(curChapter);
                        } else { //如果章节不存在则创建章节
                            BookChapterBean curChapter = new BookChapterBean();
                            curChapter.setDurChapterName(matcher.group());
                            curChapter.setStart(0L);
                            curChapter.setEnd(0L);
                            mChapterList.add(curChapter);
                        }
                    }
                }
            } else { //进行本地虚拟分章
                //章节在buffer的偏移量
                int chapterOffset = 0;
                //当前剩余可分配的长度
                int strLength = length;
                //分章的位置
                int chapterPos = 0;

                while (strLength > 0) {
                    ++chapterPos;
                    //是否长度超过一章
                    if (strLength > MAX_LENGTH_WITH_NO_CHAPTER) {
                        //在buffer中一章的终止点
                        int end = length;
                        //寻找换行符作为终止点
                        for (int i = chapterOffset + MAX_LENGTH_WITH_NO_CHAPTER; i < length; ++i) {
                            if (buffer[i] == BLANK) {
                                end = i;
                                break;
                            }
                        }
                        BookChapterBean chapter = new BookChapterBean();
                        chapter.setDurChapterName("第" + blockPos + "章" + "(" + chapterPos + ")");
                        chapter.setStart(curOffset + chapterOffset + 1);
                        chapter.setEnd(curOffset + end);
                        mChapterList.add(chapter);
                        //减去已经被分配的长度
                        strLength = strLength - (end - chapterOffset);
                        //设置偏移的位置
                        chapterOffset = end;
                    } else {
                        BookChapterBean chapter = new BookChapterBean();
                        chapter.setDurChapterName("第" + blockPos + "章" + "(" + chapterPos + ")");
                        chapter.setStart(curOffset + chapterOffset + 1);
                        chapter.setEnd(curOffset + length);
                        mChapterList.add(chapter);
                        strLength = 0;
                    }
                }
            }

            //block的偏移点
            curOffset += length;

            if (hasChapter) {
                //设置上一章的结尾
                BookChapterBean lastChapter = mChapterList.get(mChapterList.size() - 1);
                lastChapter.setEnd(curOffset);
            }

            //当添加的block太多的时候，执行GC
            if (blockPos % 15 == 0) {
                System.gc();
                System.runFinalization();
            }
        }

        for (int i = 0; i < mChapterList.size(); i++) {
            BookChapterBean bean = mChapterList.get(i);
            bean.setDurChapterIndex(i);
            bean.setNoteUrl(book.getNoteUrl());
            bean.setDurChapterUrl(MD5Utils.strToMd5By16(mBookFile.getAbsolutePath() + i + bean.getDurChapterName()));
        }
        IOUtils.close(bookStream);

        System.gc();
        System.runFinalization();

        return mChapterList;
    }

}

package com.kunfei.bookshelf.widget.page;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.text.TextUtils;

import com.kunfei.bookshelf.base.observer.SimpleObserver;
import com.kunfei.bookshelf.bean.BookShelfBean;
import com.kunfei.bookshelf.bean.ChapterListBean;
import com.kunfei.bookshelf.help.BookshelfHelp;
import com.kunfei.bookshelf.help.FormatWebText;
import com.kunfei.bookshelf.utils.EncodingDetect;
import com.kunfei.bookshelf.utils.RxUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.MediaType;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;

public class PageLoaderEpub extends PageLoader {

    //编码类型
    private Charset mCharset;

    private Book book;

    private List<ChapterListBean> chapterList;

    PageLoaderEpub(PageView pageView, BookShelfBean bookShelfBean) {
        super(pageView, bookShelfBean);
    }

    public static Book readBook(File file) {
        try {
            EpubReader epubReader = new EpubReader();
            MediaType[] lazyTypes = {
                    MediatypeService.CSS,
                    MediatypeService.GIF,
                    MediatypeService.JPG,
                    MediatypeService.PNG,
                    MediatypeService.MP3,
                    MediatypeService.MP4};

            return epubReader.readEpubLazy(file.getAbsolutePath(), "utf-8", Arrays.asList(lazyTypes));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void extractScaledCoverImage() {
        try {
            byte[] data = book.getCoverImage().getData();
            Bitmap rawCover = BitmapFactory.decodeByteArray(data, 0, data.length);
            int width = rawCover.getWidth();
            int height = rawCover.getHeight();
            if (width <= mVisibleWidth && width >= 0.8 * mVisibleWidth) {
                cover = rawCover;
                return;
            }
            height = Math.round(mVisibleWidth * 1.0f * height / width);
            cover = Bitmap.createScaledBitmap(rawCover, mVisibleWidth, height, true);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void drawCover(Canvas canvas, float top) {
        if (cover == null) {
            extractScaledCoverImage();
        }
        if (cover == null)
            return;
        int left = (mDisplayWidth - cover.getWidth()) / 2;
        canvas.drawBitmap(cover, left, top, mTextPaint);
    }

    private List<ChapterListBean> loadChapters() {
        BookShelfBean bookShelf = bookShelfBean;
        Metadata metadata = book.getMetadata();
        bookShelf.getBookInfoBean().setName(metadata.getFirstTitle());
        if (metadata.getAuthors().size() > 0) {
            String author = metadata.getAuthors().get(0).toString().replaceAll("^, |, $", "");
            bookShelf.getBookInfoBean().setAuthor(FormatWebText.getAuthor(author));
        }
        if (metadata.getDescriptions().size() > 0) {
            bookShelf.getBookInfoBean().setIntroduce(Jsoup.parse(metadata.getDescriptions().get(0)).text());
        }
        chapterList = new ArrayList<>();
        List<TOCReference> refs = book.getTableOfContents().getTocReferences();
        if (refs == null || refs.isEmpty()) {
            List<SpineReference> spineReferences = book.getSpine().getSpineReferences();
            for (int i = 0, size = spineReferences.size(); i < size; i++) {
                Resource resource = spineReferences.get(i).getResource();
                String title = resource.getTitle();
                if (TextUtils.isEmpty(title)) {
                    try {
                        Document doc = Jsoup.parse(new String(resource.getData(), mCharset));
                        Elements elements = doc.getElementsByTag("title");
                        if (elements.size() > 0) {
                            title = elements.get(0).text();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                ChapterListBean bean = new ChapterListBean();
                bean.setDurChapterIndex(i);
                bean.setNoteUrl(bookShelf.getNoteUrl());
                bean.setDurChapterUrl(resource.getHref());
                if (i == 0 && title.isEmpty()) {
                    bean.setDurChapterName("封面");
                } else {
                    bean.setDurChapterName(title);
                }
                chapterList.add(bean);
            }
        } else {
            parseMenu(refs, 0);
            for (int i = 0; i < chapterList.size(); i++) {
                chapterList.get(i).setDurChapterIndex(i);
            }
        }

        return chapterList;
    }

    private void parseMenu(List<TOCReference> refs, int level) {
        if (refs == null) return;
        for (TOCReference ref : refs) {
            if (ref.getResource() != null) {
                ChapterListBean chapterListBean = new ChapterListBean();
                chapterListBean.setNoteUrl(bookShelfBean.getNoteUrl());
                chapterListBean.setDurChapterName(ref.getTitle());
                chapterListBean.setDurChapterUrl(ref.getCompleteHref());
                chapterList.add(chapterListBean);
            }
            if (ref.getChildren() != null && !ref.getChildren().isEmpty()) {
                parseMenu(ref.getChildren(), level + 1);
            }
        }
    }

    @Override
    protected String getChapterContent(ChapterListBean chapter) throws Exception {
        Resource resource = book.getResources().getByHref(chapter.getDurChapterUrl());
        StringBuilder content = new StringBuilder();
        Document doc = Jsoup.parse(new String(resource.getData(), mCharset));
        Elements elements = doc.getAllElements();
        for (Element element : elements) {
            List<TextNode> contentEs = element.textNodes();
            for (int i = 0; i < contentEs.size(); i++) {
                String text = contentEs.get(i).text().trim();
                text = FormatWebText.getContent(text);
                if (elements.size() > 1) {
                    if (text.length() > 0) {
                        if (content.length() > 0) {
                            content.append("\r\n");
                        }
                        content.append("\u3000\u3000").append(text);
                    }
                } else {
                    content.append(text);
                }
            }
        }
        return content.toString();
    }

    private Observable<BookShelfBean> checkChapterList(BookShelfBean collBook) {
        if (!collBook.getHasUpdate() && !collBook.realChapterListEmpty()) {
            return Observable.just(collBook);
        } else {
            return Observable.create((ObservableOnSubscribe<List<ChapterListBean>>) e -> {
                List<ChapterListBean> chapterList = loadChapters();
                if (!chapterList.isEmpty()) {
                    e.onNext(chapterList);
                } else {
                    e.onError(new IllegalAccessException("book sub-chapter failed!"));
                }
                e.onComplete();
            })
                    .flatMap(chapterList -> {
                        collBook.setChapterList(chapterList);
                        collBook.setChapterListSize(chapterList.size());
                        return Observable.just(collBook);
                    })
                    .doOnNext(bookShelfBean -> {
                        // 存储章节到数据库
                        bookShelfBean.setHasUpdate(false);
                        bookShelfBean.setFinalRefreshData(System.currentTimeMillis());
                        if (BookshelfHelp.isInBookShelf(bookShelfBean.getNoteUrl())) {
                            BookshelfHelp.saveBookToShelf(bookShelfBean);
                        }
                    });
        }
    }

    @Override
    public void refreshChapterList() {
        BookShelfBean bookShelf = bookShelfBean;
        if (bookShelf == null) return;

        Observable.create((ObservableOnSubscribe<BookShelfBean>) e -> {
            File bookFile = new File(bookShelf.getNoteUrl());
            book = readBook(bookFile);

            if (book == null) {
                e.onError(new Exception("文件解析失败"));
                return;
            }
            if (TextUtils.isEmpty(bookShelf.getBookInfoBean().getCharset())) {
                bookShelf.getBookInfoBean().setCharset(EncodingDetect.getEncodeInHtml(book.getCoverPage().getData()));
            }
            mCharset = Charset.forName(bookShelf.getBookInfoBean().getCharset());

            e.onNext(bookShelf);
            e.onComplete();
        }).subscribeOn(Schedulers.single())
                .flatMap(this::checkChapterList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<BookShelfBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        isChapterListPrepare = true;

                        //提示目录加载完成
                        if (mPageChangeListener != null) {
                            mPageChangeListener.onCategoryFinish(bookShelfBean.getChapterList());
                        }

                        // 加载并显示当前章节
                        skipToChapter(bookShelfBean.getDurChapter(), bookShelfBean.getDurChapterPage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        chapterError(e.getMessage());
                    }
                });
    }

    @Override
    protected boolean noChapterData(ChapterListBean chapter) {
        return false;
    }

    @Override
    public void updateChapter() {
        BookShelfBean bookShelf = bookShelfBean;
        mPageView.getActivity().toast("目录更新中");
        Observable.create((ObservableOnSubscribe<BookShelfBean>) e->{
            bookShelf.setChapterList(null);
            BookshelfHelp.delChapterList(bookShelf.getNoteUrl());
            if (TextUtils.isEmpty(bookShelf.getBookInfoBean().getCharset())) {
                bookShelf.getBookInfoBean().setCharset(EncodingDetect.getEncodeInHtml(book.getCoverPage().getData()));
            }
            mCharset = Charset.forName(bookShelf.getBookInfoBean().getCharset());
            e.onNext(bookShelf);
            e.onComplete();
        }).flatMap(this::checkChapterList)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new Observer<BookShelfBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        mPageView.getActivity().toast("更新完成");
                        isChapterListPrepare = true;

                        //提示目录加载完成
                        if (mPageChangeListener != null) {
                            mPageChangeListener.onCategoryFinish(bookShelfBean.getChapterList());
                        }

                        // 加载并显示当前章节
                        skipToChapter(bookShelfBean.getDurChapter(), bookShelfBean.getDurChapterPage());
                    }

                    @Override
                    public void onError(Throwable e) {
                        chapterError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}

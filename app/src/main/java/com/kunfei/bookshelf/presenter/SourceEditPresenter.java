package com.kunfei.bookshelf.presenter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kunfei.basemvplib.BasePresenterImpl;
import com.kunfei.basemvplib.impl.IView;
import com.kunfei.bookshelf.DbHelper;
import com.kunfei.bookshelf.bean.BookSource3Bean;
import com.kunfei.bookshelf.bean.BookSourceBean;
import com.kunfei.bookshelf.model.BookSourceManager;
import com.kunfei.bookshelf.presenter.contract.SourceEditContract;
import com.kunfei.bookshelf.utils.RxUtils;

import java.util.List;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by GKF on 2018/1/28.
 * 编辑书源
 */
public class SourceEditPresenter extends BasePresenterImpl<SourceEditContract.View> implements SourceEditContract.Presenter {

    @Override
    public Observable<Boolean> saveSource(BookSourceBean bookSource, BookSourceBean bookSourceOld) {
        return Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            if (!TextUtils.isEmpty(bookSourceOld.getBookSourceUrl()) && !Objects.equals(bookSource.getBookSourceUrl(), bookSourceOld.getBookSourceUrl())) {
                DbHelper.getDaoSession().getBookSourceBeanDao().delete(bookSourceOld);
            }
            BookSourceManager.addBookSource(bookSource);
            e.onNext(true);
        }).compose(RxUtils::toSimpleSingle);
    }

    @Override
    public void copySource(String bookSource) {
        ClipboardManager clipboard = (ClipboardManager) mView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, bookSource);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clipData);
        }
    }

    @Override
    public void pasteSource() {
        ClipboardManager clipboard = (ClipboardManager) mView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard != null ? clipboard.getPrimaryClip() : null;
        if (clipData != null && clipData.getItemCount() > 0) {
            setText(String.valueOf(clipData.getItemAt(0).getText()));
        }
    }

    @Override
    public void setText(String bookSourceStr) {
        try {
            if (bookSourceStr.trim().length() > 5) {
                mView.setText( mathcSourceBean(bookSourceStr.trim())  );
            } else {
                mView.toast("似乎不是书源内容");
                // 理论上这里已经没用了
//                Gson gson = new Gson();
//                BookSourceBean bookSourceBean = gson.fromJson(bookSourceStr, BookSourceBean.class);
//                mView.setText(bookSourceBean);
            }
        } catch (Exception e) {
            mView.toast("数据格式不对");
            e.printStackTrace();
        }
    }

    private BookSourceBean mathcSourceBean(String str) {
        Gson gson = new Gson();
        BookSource3Bean bookSource3Bean=new BookSource3Bean();
        BookSourceBean bookSource2Bean=new BookSourceBean();
        int r2 = 0, r3 = 0;
        try {
            if (str.charAt(0) == '[' && str.charAt(str.length() - 1) == ']') {
                List<BookSource3Bean> list = gson.fromJson(str, new TypeToken<List<BookSource3Bean>>() {
                }.getType());
                bookSource3Bean = list.get(0);
            } else {
                bookSource3Bean = gson.fromJson(str, BookSource3Bean.class);
            }
            r3 = gson.toJson(bookSource3Bean).length();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (str.charAt(0) == '[' && str.charAt(str.length() - 1) == ']') {
                List<BookSourceBean> list = gson.fromJson(str, new TypeToken<List<BookSourceBean>>() {
                }.getType());
                bookSource2Bean = list.get(0);
            } else {
                bookSource2Bean = gson.fromJson(str, BookSourceBean.class);
            }
            r2 = gson.toJson(bookSource2Bean).length();
            // r2 r3的计算在调用searchUrl2RuleSearchUrl() 等高级转换方法之前，是简化算法的粗糙的做法
            if (r2 > r3)
                return bookSource2Bean;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (r3 > 0) {
            mView.toast("导入了阅读3.0书源。如有Bug请及时上报");
            return bookSource3Bean.addGroupTag("阅读3.0书源").toBookSourceBean();
        }
        return bookSource2Bean;
    }

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
    }

    @Override
    public void detachView() {

    }
}

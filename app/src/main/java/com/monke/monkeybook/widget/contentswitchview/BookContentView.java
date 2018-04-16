package com.monke.monkeybook.widget.contentswitchview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monke.monkeybook.R;
import com.monke.monkeybook.help.ACache;
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.widget.ContentTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.grantland.widget.AutofitTextView;

public class BookContentView extends FrameLayout {
    public long qTag = System.currentTimeMillis();

    public static final int DurPageIndexBegin = -1;
    public static final int DurPageIndexEnd = -2;

    @BindView(R.id.iv_bg)
    ImageView ivBg;
    @BindView(R.id.tv_title)
    AutofitTextView tvTitle;
    @BindView(R.id.tv_page)
    TextView tvPage;
    @BindView(R.id.v_bottom)
    View vBottom;
    @BindView(R.id.tv_content)
    ContentTextView tvContent;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.tv_loading)
    TextView tvLoading;
    @BindView(R.id.tv_error_info)
    TextView tvErrorInfo;
    @BindView(R.id.tv_load_again)
    TextView tvLoadAgain;
    @BindView(R.id.ll_error)
    LinearLayout llError;

    private SharedPreferences preferences;

    private double textHeigth = 0;

    private String title;
    private String content;
    private int durChapterIndex;
    private int chapterAll;
    private int durPageIndex;      //如果durPageIndex = -1 则是从头开始  -2则是从尾开始
    private int pageAll;

    private ContentSwitchView.LoadDataListener loadDataListener;

    private SetDataListener setDataListener;

    public interface SetDataListener {
        void setDataFinish(BookContentView bookContentView, int durChapterIndex, int chapterAll, int durPageIndex, int pageAll, int fromPageIndex);
    }

    public BookContentView(Context context) {
        this(context, null);
    }

    public BookContentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BookContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BookContentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init() {
        View view;
        if (preferences.getBoolean("hide_status_bar", false)) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_content_hide_status_bar, this, false);
        } else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_content_show_status_bar, this, false);
        }
        addView(view);
        ButterKnife.bind(this, view);

        tvLoadAgain.setOnClickListener(v -> {
            if (loadDataListener != null)
                loading();
        });
    }

    public void showLoading() {
        llError.setVisibility(GONE);
        tvLoading.setVisibility(VISIBLE);
        llContent.setVisibility(INVISIBLE);
    }

    public void loading() {
        llError.setVisibility(GONE);
        tvLoading.setVisibility(VISIBLE);
        llContent.setVisibility(INVISIBLE);
        qTag = System.currentTimeMillis();
        //执行请求操作
        if (loadDataListener != null) {
            loadDataListener.loadData(this, qTag, durChapterIndex, durPageIndex);
        }
    }

    public void finishLoading() {
        llError.setVisibility(GONE);
        llContent.setVisibility(VISIBLE);
        tvLoading.setVisibility(GONE);
    }

    @SuppressLint("DefaultLocale")
    public void setNoData(String contentLines) {
        this.content = contentLines;
        tvPage.setText(String.format("%d/%d", this.durPageIndex + 1, this.pageAll));
        finishLoading();
    }

    public void updateData(long tag, String title, List<String> contentLines, int durChapterIndex, int chapterAll, int durPageIndex, int durPageAll) {
        if (tag == qTag) {

            if (contentLines == null) {
                this.content = "";
            } else {
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < contentLines.size(); i++) {
                    s.append(contentLines.get(i));
                }
                this.content = s.toString();
            }
            this.title = title;
            this.durChapterIndex = durChapterIndex;
            this.chapterAll = chapterAll;
            this.durPageIndex = durPageIndex;
            this.pageAll = durPageAll;

            tvTitle.setText(this.title);

            /*SpannableStringBuilder spanBuilder = new SpannableStringBuilder(this.content);
            spanBuilder.setSpan(new TextAppearanceSpan(null, 0, (int)tvContent.getTextSize()+10, null, null),
                    0, TitleSize, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            tvContent.setText(spanBuilder);*/
            tvContent.setText(this.content);
            tvPage.setText(String.format("%d/%d", this.durPageIndex + 1, this.pageAll));

            if (setDataListener != null) {
                setDataListener.setDataFinish(this, durChapterIndex, chapterAll, durPageIndex, durPageAll, this.durPageIndex);
            }
            finishLoading();
        }
    }

    public void loadData(String title, int durChapterIndex, int chapterAll, int durPageIndex) {
        this.title = title;
        this.durChapterIndex = durChapterIndex;
        this.chapterAll = chapterAll;
        this.durPageIndex = durPageIndex;
        tvTitle.setText(title);
        tvPage.setText("");

        loading();

    }

    public ContentSwitchView.LoadDataListener getLoadDataListener() {
        return loadDataListener;
    }

    public void setLoadDataListener(ContentSwitchView.LoadDataListener loadDataListener, SetDataListener setDataListener) {
        this.loadDataListener = loadDataListener;
        this.setDataListener = setDataListener;
    }

    public void setLoadDataListener(ContentSwitchView.LoadDataListener loadDataListener) {
        this.loadDataListener = loadDataListener;
    }

    public void loadError() {
        llError.setVisibility(VISIBLE);
        tvLoading.setVisibility(GONE);
        llContent.setVisibility(INVISIBLE);
    }

    public int getPageAll() {
        return pageAll;
    }

    public void setPageAll(int pageAll) {
        this.pageAll = pageAll;
    }

    public int getDurPageIndex() {
        return durPageIndex;
    }

    public void setDurPageIndex(int durPageIndex) {
        this.durPageIndex = durPageIndex;
    }

    public int getDurChapterIndex() {
        return durChapterIndex;
    }

    public void setDurChapterIndex(int durChapterIndex) {
        this.durChapterIndex = durChapterIndex;
    }

    public int getChapterAll() {
        return chapterAll;
    }

    public void setChapterAll(int chapterAll) {
        this.chapterAll = chapterAll;
    }

    public SetDataListener getSetDataListener() {
        return setDataListener;
    }

    public void setSetDataListener(SetDataListener setDataListener) {
        this.setDataListener = setDataListener;
    }

    public double getTextHeigth() {
        return textHeigth;
    }

    public void setTextHeigth(double textHeigth) {
        this.textHeigth = textHeigth;
    }

    public long getQTag() {
        return qTag;
    }

    public void setQTag(long qTag) {
        this.qTag = qTag;
    }

    public TextView getTvContent() {
        return tvContent;
    }

    public String getContent() {
        return content;
    }

    //显示行数
    public int getLineCount(int height, int lineNum, ReadBookControl readBookControl) {
        Paint mTextPaint = tvContent.getPaint();
        //字体高度
        double textHeight = Math.ceil(mTextPaint.getFontMetrics().descent - mTextPaint.getFontMetrics().ascent) * 1.0f;
        textHeight = textHeight * readBookControl.getLineMultiplier() + readBookControl.getTextExtra();

        this.textHeigth = textHeight;

        //计算TextView高度
        //Log.e("LineHeight>>",tvContent.getLineHeight()+"---"+textHeight);
        //行间距
        return (int) (height * 1.0f / tvContent.getLineHeight() + lineNum);
        //return (int) (height * 1.0f / textHeigth + lineNum);
    }


    public void setReadBookControl(ReadBookControl readBookControl) {
        setFont(readBookControl);
        setTextBold(readBookControl);
        setTextKind(readBookControl);
        setBg(readBookControl);
    }

    public void setBg(ReadBookControl readBookControl) {
        if (readBookControl.getTextDrawableIndex() != -1 || readBookControl.getIsNightTheme()) {
            ivBg.setImageResource(readBookControl.getTextBackground());
            tvTitle.setTextColor(readBookControl.getTextColor());
            tvContent.setTextColor(readBookControl.getTextColor());
            tvPage.setTextColor(readBookControl.getTextColor());
            vBottom.setBackgroundColor(readBookControl.getTextColor());
            tvLoading.setTextColor(readBookControl.getTextColor());
            tvErrorInfo.setTextColor(readBookControl.getTextColor());
        } else {
            ACache aCache = ACache.get(this.getContext());
            ivBg.setImageBitmap(aCache.getAsBitmap("customBg"));
            tvTitle.setTextColor(readBookControl.getTextColorCustom());
            tvContent.setTextColor(readBookControl.getTextColorCustom());
            tvPage.setTextColor(readBookControl.getTextColorCustom());
            vBottom.setBackgroundColor(readBookControl.getTextColorCustom());
            tvLoading.setTextColor(readBookControl.getTextColorCustom());
            tvErrorInfo.setTextColor(readBookControl.getTextColorCustom());
        }
    }

    public void setFont(ReadBookControl readBookControl) {
        //自定义字体
        try {
            if (readBookControl.getFontPath() != null || "".equals(readBookControl.getFontPath())) {
                Typeface typeface = Typeface.createFromFile(readBookControl.getFontPath());
                tvContent.setTypeface(typeface);
                tvTitle.setTypeface(typeface);
                tvContent.setFont();
            } else {
                tvContent.setTypeface(Typeface.SANS_SERIF);
                tvTitle.setTypeface(Typeface.SANS_SERIF);
            }
        } catch (Exception e) {
            Toast.makeText(this.getContext(), "字体文件未找,到恢复默认字体", Toast.LENGTH_SHORT).show();
            readBookControl.setReadBookFont(null);
            tvContent.setTypeface(Typeface.SANS_SERIF);
            tvTitle.setTypeface(Typeface.SANS_SERIF);
        }
    }

    /**
     * 字体加粗
     */
    public void setTextBold(ReadBookControl readBookControl) {
        TextPaint tp = tvContent.getPaint();
        if (readBookControl.getTextBold()) {
            tp.setFakeBoldText(true);
            tvContent.setText(tvContent.getText());
        } else {
            tp.setFakeBoldText(false);
            tvContent.setText(tvContent.getText());
        }

    }

    public void setTextKind(ReadBookControl readBookControl) {

        tvContent.setTextSize(readBookControl.getTextSize());
        tvContent.setLineSpacing(readBookControl.getTextExtra(), readBookControl.getLineMultiplier());
    }


}

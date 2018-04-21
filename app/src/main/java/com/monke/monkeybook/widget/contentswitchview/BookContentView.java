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
import com.monke.monkeybook.utils.BatteryUtil;
import com.monke.monkeybook.widget.ContentTextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookContentView extends FrameLayout {
    public long qTag = System.currentTimeMillis();

    public static final int DurPageIndexBegin = -1;
    public static final int DurPageIndexEnd = -2;

    @BindView(R.id.iv_bg)
    ImageView ivBg;
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
    @BindView(R.id.tvTopLeft)
    TextView tvTopLeft;
    @BindView(R.id.tvTopRight)
    TextView tvTopRight;
    @BindView(R.id.llTop)
    LinearLayout llTop;
    @BindView(R.id.v_top)
    View vTop;
    @BindView(R.id.v_bottom)
    View vBottom;
    @BindView(R.id.tvBottomLeft)
    TextView tvBottomLeft;
    @BindView(R.id.tvBottomRight)
    TextView tvBottomRight;
    @BindView(R.id.llBottom)
    LinearLayout llBottom;

    private String content;
    private int durChapterIndex;
    private int chapterAll;
    private int durPageIndex;      //如果durPageIndex = -1 则是从头开始  -2则是从尾开始
    private int pageAll;
    private boolean hideStatusBar;
    private ReadBookControl readBookControl;

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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        init(preferences.getBoolean("hide_status_bar", false));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BookContentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        init(preferences.getBoolean("hide_status_bar", false));
    }

    public void init(boolean hideStatus) {
        this.hideStatusBar = hideStatus;
        View view;
        if (hideStatus) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_content_horizontal2, this, false);
        } else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_content_horizontal1, this, false);
        }
        addView(view);
        ButterKnife.bind(this, view);
        if (hideStatus) {
            llTop.setVisibility(VISIBLE);
            vTop.setVisibility(VISIBLE);
            vBottom.setVisibility(GONE);
        }
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
        finishLoading();
    }

    @SuppressLint("DefaultLocale")
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

            this.durChapterIndex = durChapterIndex;
            this.chapterAll = chapterAll;
            this.durPageIndex = durPageIndex;
            this.pageAll = durPageAll;

            tvContent.setText(this.content);
            if (hideStatusBar) {
                tvTopLeft.setText(title);
                tvTopRight.setText(String.format("%d/%d", durPageIndex + 1, pageAll));
                @SuppressLint("SimpleDateFormat")
                DateFormat dfTime = new SimpleDateFormat("HH:mm");
                tvBottomLeft.setText(dfTime.format(Calendar.getInstance().getTime()));
                tvBottomRight.setText(String.format("%d%%", BatteryUtil.getLevel(getContext())));
                readBookControl = ReadBookControl.getInstance();
                if (!readBookControl.getShowTimeBattery()) {
                    llBottom.setVisibility(GONE);
                    vBottom.setVisibility(GONE);
                }
                tvTopLeft.setOnClickListener(view -> {
                    ContentSwitchView csv = (ContentSwitchView) getParent();
                    csv.openChapterList();
                });
            } else {
                tvBottomLeft.setText(title);
                tvBottomRight.setText(String.format("%d/%d", durPageIndex + 1, pageAll));
                tvBottomLeft.setOnClickListener(view -> {
                    ContentSwitchView csv = (ContentSwitchView) getParent();
                    csv.openChapterList();
                });
            }

            if (setDataListener != null) {
                setDataListener.setDataFinish(this, durChapterIndex, chapterAll, durPageIndex, durPageAll, this.durPageIndex);
            }
            finishLoading();
        }
    }

    public void loadData(String title, int durChapterIndex, int chapterAll, int durPageIndex) {

        this.durChapterIndex = durChapterIndex;
        this.chapterAll = chapterAll;
        this.durPageIndex = durPageIndex;

        loading();
    }

    public void setLoadDataListener(ContentSwitchView.LoadDataListener loadDataListener, SetDataListener setDataListener) {
        this.loadDataListener = loadDataListener;
        this.setDataListener = setDataListener;
    }

    public void loadError(String errorMsg) {
        if (errorMsg != null) {
            tvErrorInfo.setText(errorMsg);
        }
        llError.setVisibility(VISIBLE);
        tvLoading.setVisibility(GONE);
        llContent.setVisibility(INVISIBLE);
    }

    public int getPageAll() {
        return pageAll;
    }

    public int getDurPageIndex() {
        return durPageIndex;
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

    public long getQTag() {
        return qTag;
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
        //行间距
        double textSpacing = textHeight * readBookControl.getLineMultiplier() - textHeight + readBookControl.getTextExtra();

//        Log.e("LineHeight>>",tvContent.getLineHeight() + " " + textHeight + " " + textSpacing + "  " + height);
        //行数
        double lineCount = (height) * 1.0f / tvContent.getLineHeight() + lineNum;
//        Log.e("LineCount>>", String.valueOf(lineCount));
        return (int) lineCount;
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
            tvContent.setTextColor(readBookControl.getTextColor());
            tvLoading.setTextColor(readBookControl.getTextColor());
            tvErrorInfo.setTextColor(readBookControl.getTextColor());
            tvTopLeft.setTextColor(readBookControl.getTextColor());
            tvTopRight.setTextColor(readBookControl.getTextColor());
            vTop.setBackgroundColor(readBookControl.getTextColor());
            vBottom.setBackgroundColor(readBookControl.getTextColor());
            tvBottomLeft.setTextColor(readBookControl.getTextColor());
            tvBottomRight.setTextColor(readBookControl.getTextColor());
        } else {
            ACache aCache = ACache.get(this.getContext());
            ivBg.setImageBitmap(aCache.getAsBitmap("customBg"));
            tvContent.setTextColor(readBookControl.getTextColorCustom());
            tvLoading.setTextColor(readBookControl.getTextColorCustom());
            tvErrorInfo.setTextColor(readBookControl.getTextColorCustom());
            tvTopLeft.setTextColor(readBookControl.getTextColorCustom());
            tvTopRight.setTextColor(readBookControl.getTextColorCustom());
            vTop.setBackgroundColor(readBookControl.getTextColorCustom());
            vBottom.setBackgroundColor(readBookControl.getTextColorCustom());
            tvBottomLeft.setTextColor(readBookControl.getTextColorCustom());
            tvBottomRight.setTextColor(readBookControl.getTextColorCustom());
        }
    }

    public void setFont(ReadBookControl readBookControl) {
        //自定义字体
        try {
            if (readBookControl.getFontPath() != null || "".equals(readBookControl.getFontPath())) {
                Typeface typeface = Typeface.createFromFile(readBookControl.getFontPath());
                tvContent.setTypeface(typeface);
                tvContent.setFont();
                tvTopLeft.setTypeface(typeface);
                tvTopRight.setTypeface(typeface);
                tvBottomLeft.setTypeface(typeface);
                tvBottomRight.setTypeface(typeface);
            } else {
                tvContent.setTypeface(Typeface.SANS_SERIF);
                tvTopLeft.setTypeface(Typeface.SANS_SERIF);
                tvTopRight.setTypeface(Typeface.SANS_SERIF);
                tvBottomLeft.setTypeface(Typeface.SANS_SERIF);
                tvBottomRight.setTypeface(Typeface.SANS_SERIF);
            }
        } catch (Exception e) {
            Toast.makeText(this.getContext(), "字体文件未找,到恢复默认字体", Toast.LENGTH_SHORT).show();
            readBookControl.setReadBookFont(null);
            tvContent.setTypeface(Typeface.SANS_SERIF);
            tvTopLeft.setTypeface(Typeface.SANS_SERIF);
            tvTopRight.setTypeface(Typeface.SANS_SERIF);
            tvBottomLeft.setTypeface(Typeface.SANS_SERIF);
            tvBottomRight.setTypeface(Typeface.SANS_SERIF);
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

    public void setTime(String time) {
        tvBottomLeft.setText(time);
    }

    public void setBattery(String battery) {
        tvBottomRight.setText(battery);
    }
}

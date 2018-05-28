package com.monke.monkeybook.widget.contentswitchview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
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
import com.monke.monkeybook.help.ReadBookControl;
import com.monke.monkeybook.utils.BatteryUtil;
import com.monke.monkeybook.utils.barUtil.ImmersionBar;
import com.monke.monkeybook.widget.BatteryView;
import com.monke.monkeybook.widget.ContentTextView;

import java.text.DateFormat;
import java.text.DecimalFormat;
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
    @BindView(R.id.tvTime)
    TextView tvTime;
    @BindView(R.id.tvProgress)
    TextView tvProgress;
    @BindView(R.id.vwBattery)
    BatteryView vwBattery;
    @BindView(R.id.llTop)
    LinearLayout llTop;
    @BindView(R.id.vwLine)
    View vwLine;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvPage)
    TextView tvPage;
    @BindView(R.id.llBottom)
    View llBottom;

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
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BookContentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init() {
        readBookControl = ReadBookControl.getInstance();
        hideStatusBar = readBookControl.getHideStatusBar();
        Activity activity = (Activity) getContext();
        View view;
        if (hideStatusBar) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_content_horizontal2, this, false);
        } else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.adapter_content_horizontal1, this, false);
        }
        addView(view);
        ButterKnife.bind(this, view);
        if (hideStatusBar) {
            llTop.setVisibility(VISIBLE);
        } else {
            llTop.setPadding(0, ImmersionBar.getStatusBarHeight(activity), 0, 0);
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
            tvTitle.setText(title);
            tvPage.setText(String.format("%d/%d", durPageIndex + 1, pageAll));
            if (chapterAll > 0) {
                DecimalFormat df = new DecimalFormat("0.00%");
                tvProgress.setText(df.format(durChapterIndex * 1.0f / chapterAll + 1.0f / chapterAll * (durPageIndex + 1) / durPageAll));
            }
            tvTitle.setOnClickListener(view -> {
                ContentSwitchView csv = (ContentSwitchView) getParent();
                csv.openChapterList();
            });
            if (hideStatusBar) {
                @SuppressLint("SimpleDateFormat")
                DateFormat dfTime = new SimpleDateFormat("HH:mm");
                tvTime.setText(dfTime.format(Calendar.getInstance().getTime()));
                vwBattery.setVisibility(VISIBLE);
                vwBattery.setPower(BatteryUtil.getLevel(getContext()));
                if (!readBookControl.getShowTimeBattery()) {
                    llBottom.setVisibility(GONE);
                }
            }
            if (readBookControl.getShowLine()) {
                vwLine.setVisibility(VISIBLE);
            } else {
                vwLine.setVisibility(INVISIBLE);
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
    public int getLineCount(int height) {
        //行数
        double lineCount = (height) * 1.0f / tvContent.getLineHeight();
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
        ivBg.setImageDrawable(readBookControl.getTextBackground());
        tvContent.setTextColor(readBookControl.getTextColor());
        tvLoading.setTextColor(readBookControl.getTextColor());
        tvErrorInfo.setTextColor(readBookControl.getTextColor());
        tvTitle.setTextColor(readBookControl.getTextColor());
        tvTime.setTextColor(readBookControl.getTextColor());
        tvPage.setTextColor(readBookControl.getTextColor());
        tvProgress.setTextColor(readBookControl.getTextColor());
        vwLine.setBackgroundColor(readBookControl.getTextColor());
        vwBattery.setColor(readBookControl.getTextColor());
    }

    public void setFont(ReadBookControl readBookControl) {
        //自定义字体
        try {
            if (readBookControl.getFontPath() != null || "".equals(readBookControl.getFontPath())) {
                Typeface typeface = Typeface.createFromFile(readBookControl.getFontPath());
                tvContent.setTypeface(typeface);
                tvContent.invalidate();
                tvPage.setTypeface(typeface);
                tvProgress.setTypeface(typeface);
                tvTitle.setTypeface(typeface);
                tvTime.setTypeface(typeface);
            } else {
                tvContent.setTypeface(Typeface.SANS_SERIF);
                tvContent.invalidate();
                tvTitle.setTypeface(Typeface.SANS_SERIF);
                tvTime.setTypeface(Typeface.SANS_SERIF);
                tvPage.setTypeface(Typeface.SANS_SERIF);
                tvProgress.setTypeface(Typeface.SANS_SERIF);
            }
        } catch (Exception e) {
            Toast.makeText(this.getContext(), "字体文件未找,到恢复默认字体", Toast.LENGTH_SHORT).show();
            readBookControl.setReadBookFont(null);
            tvContent.setTypeface(Typeface.SANS_SERIF);
            tvContent.invalidate();
            tvTitle.setTypeface(Typeface.SANS_SERIF);
            tvTime.setTypeface(Typeface.SANS_SERIF);
            tvPage.setTypeface(Typeface.SANS_SERIF);
            tvProgress.setTypeface(Typeface.SANS_SERIF);
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
        tvTime.setText(time);
    }

    public void setBattery(Integer battery) {
        vwBattery.setPower(battery);
    }

    public void upSpeak(SpannableString ssContent) {
        tvContent.setText(ssContent);
    }

    public void resetContent() {
        tvContent.setText(content);
    }
}

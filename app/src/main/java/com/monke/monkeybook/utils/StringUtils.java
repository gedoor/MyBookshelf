package com.monke.monkeybook.utils;

import android.support.annotation.StringRes;

import com.example.newbiechen.ireader.App;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by newbiechen on 17-4-22.
 * 对文字操作的工具类
 */

public class StringUtils {
    private static final String TAG = "StringUtils";
    private static final int HOUR_OF_DAY = 24;
    private static final int DAY_OF_YESTERDAY = 2;
    private static final int TIME_UNIT = 60;

    //将时间转换成日期
    public static String dateConvert(long time, String pattern){
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    //将日期转换成昨天、今天、明天
    public static String dateConvert(String source, String pattern){
        DateFormat format = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        try {
            Date date = format.parse(source);
            long curTime = calendar.getTimeInMillis();
            calendar.setTime(date);
            //将MISC 转换成 sec
            long difSec = Math.abs((curTime - date.getTime())/1000);
            long difMin =  difSec/60;
            long difHour = difMin/60;
            long difDate = difHour/60;
            int oldHour = calendar.get(Calendar.HOUR);
            //如果没有时间
            if (oldHour == 0){
                //比日期:昨天今天和明天
                if (difDate == 0){
                    return "今天";
                }
                else if (difDate < DAY_OF_YESTERDAY){
                    return "昨天";
                }
                else {
                    DateFormat convertFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String value = convertFormat.format(date);
                    return value;
                }
            }

            if (difSec < TIME_UNIT){
                return difSec+"秒前";
            }
            else if (difMin < TIME_UNIT){
                return difMin+"分钟前";
            }
            else if (difHour < HOUR_OF_DAY){
                return difHour+"小时前";
            }
            else if (difDate < DAY_OF_YESTERDAY){
                return "昨天";
            }
            else {
                DateFormat convertFormat = new SimpleDateFormat("yyyy-MM-dd");
                String value = convertFormat.format(date);
                return value;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String toFirstCapital(String str){
        return str.substring(0,1).toUpperCase()+str.substring(1);
    }

    public static String getString(@StringRes int id){
        return App.getContext().getResources().getString(id);
    }

    public static String getString(@StringRes int id, Object... formatArgs){
        return App.getContext().getResources().getString(id,formatArgs);
    }

    /**
     * 将文本中的半角字符，转换成全角字符
     * @param input
     * @return
     */
    public static String halfToFull(String input)
    {
        char[] c = input.toCharArray();
        for (int i = 0; i< c.length; i++)
        {
            if (c[i] == 32) //半角空格
            {
                c[i] = (char) 12288;
                continue;
            }
            //根据实际情况，过滤不需要转换的符号
            //if (c[i] == 46) //半角点号，不转换
            // continue;

            if (c[i]> 32 && c[i]< 127)    //其他符号都转换为全角
                c[i] = (char) (c[i] + 65248);
        }
        return new String(c);
    }

    //功能：字符串全角转换为半角
    public static String fullToHalf(String input)
    {
        char[] c = input.toCharArray();
        for (int i = 0; i< c.length; i++)
        {
            if (c[i] == 12288) //全角空格
            {
                c[i] = (char) 32;
                continue;
            }

            if (c[i]> 65280&& c[i]< 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }
}

package com.monke.monkeybook.utils.fileSelectorUtil;

import java.text.DecimalFormat;

import com.monke.monkeybook.R;

public class FileHelper {
	
	/**
	 * 根据文件名后缀返回图片资源id
	 * */
	public static int getImageBySuffix(String suffix) {
		int resource = R.drawable.ic_file_24dp;
		if (suffix.equalsIgnoreCase("txt")) {
			resource = R.drawable.ic_txt_24dp;
		}
		return resource;
	}
	
	/**
	 * 转换文件大小
	 * */
	public static String FormetFileSize(long size) {//转换文件大小
		DecimalFormat df = new DecimalFormat("#.00");
		String result = "";
		if (size < 1024) {
			result = df.format((double) size) + "B";
		} else if (size < 1048576) {
			result = df.format((double) size / 1024) + "K";
		} else if (size < 1073741824) {
			result = df.format((double) size / 1048576) + "M";
		} else {
			result = df.format((double) size / 1073741824) + "G";
		}
		return result;
	}
	
}

package com.monke.monkeybook.utils.fileSelectorUtil;

import java.io.File;
import java.util.Comparator;

/**
 * 文件夹及文件按字母排序规则类*/
public class FileComparator implements Comparator<File> {

	@Override
	public int compare(File file1, File file2) {
		if (file1.isDirectory() && file2.isDirectory()) {//1.先比较文件夹，让文件夹排在列表的最前边，并且以A-Z的字典顺序排列
			return file1.getName().compareToIgnoreCase(file2.getName());
		} else {
			if (file1.isDirectory() && file2.isFile()) {//2.比较文件夹和文件
				return -1;
			} else if (file1.isFile() && file2.isDirectory()) {//3.比较文件和文件夹
				return 1;
			} else {
				return file1.getName().compareToIgnoreCase(file2.getName());//4.比较文件
			}
		}
	}

}


package com.monke.monkeybook.utils.fileSelectorUtil;

import java.io.File;
import java.io.FileFilter;

/**
 * 去除隐藏文件，空文件过滤器
 * */
public class EmptyFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		if (file != null && !file.getName().startsWith(".") && file.length() != 0) {
			return true;
		} else {
			return false;
		}
	}

}

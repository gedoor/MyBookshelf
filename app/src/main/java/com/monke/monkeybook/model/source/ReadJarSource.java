package com.monke.monkeybook.model.source;

import com.monke.monkeybook.bean.BookSourceBean;
import com.monke.monkeybook.utils.FileUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;

public class ReadJarSource {

    ReadJarSource() {
        String dirPath = FileUtil.getSdCardPath() + "/YueDu/JarSource";
        File file = new File(dirPath);

        if (file.isDirectory()) {
            File sourceFile[] = file.listFiles(file1 -> !file1.getName().endsWith(".jar"));


        }

    }

}

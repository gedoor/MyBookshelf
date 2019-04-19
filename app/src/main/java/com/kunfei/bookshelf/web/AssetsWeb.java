package com.kunfei.bookshelf.web;

import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kunfei.bookshelf.MApplication;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.res.AssetManager.ACCESS_BUFFER;

public class AssetsWeb {
    private AssetManager assetManager;
    private String mRootPath = "web";

    AssetsWeb() {
        assetManager = MApplication.getInstance().getAssets();
    }

    public String readFile(String path) throws IOException {
        path = (mRootPath + path).replaceAll("/", File.separator);
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(path)));
        StringBuilder stringBuffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuffer.append(line);
        }
        reader.close();
        return stringBuffer.toString();
    }

}

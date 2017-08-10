package com.monke.monkeybook.presenter;

import com.monke.basemvplib.IPresenter;
import java.util.LinkedHashMap;

public interface ILibraryPresenter extends IPresenter{

    public LinkedHashMap<String, String> getKinds();

    public void getLibraryData();
}

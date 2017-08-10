package com.monke.monkeybook.view;

import com.monke.basemvplib.IView;
import com.monke.monkeybook.bean.LibraryBean;

public interface ILibraryView extends IView{

    public void updateUI(LibraryBean library);
    public void finishRefresh();
}

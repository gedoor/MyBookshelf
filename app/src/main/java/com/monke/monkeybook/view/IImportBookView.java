package com.monke.monkeybook.view;

import com.monke.basemvplib.IView;
import java.io.File;

public interface IImportBookView extends IView{

    public void addNewBook(File newFile);

    public void searchFinish();

    public void addSuccess();

    public void addError();
}
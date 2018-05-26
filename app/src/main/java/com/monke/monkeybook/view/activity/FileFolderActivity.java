package com.monke.monkeybook.view.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.MyFile;
import com.monke.monkeybook.presenter.ImportBookPresenterImpl;
import com.monke.monkeybook.presenter.impl.IImportBookPresenter;
import com.monke.monkeybook.utils.fileSelectorUtil.EmptyFileFilter;
import com.monke.monkeybook.utils.fileSelectorUtil.FileComparator;
import com.monke.monkeybook.utils.fileSelectorUtil.SDCardScanner;
import com.monke.monkeybook.view.adapter.FileFolderListAdapter;
import com.monke.monkeybook.view.impl.IImportBookView;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileFolderActivity extends MBaseActivity<IImportBookPresenter> implements OnItemClickListener,IImportBookView {
	private FileFolderActivity instance = this;
	private ArrayList<MyFile> data = new ArrayList<>();
	private FileFolderListAdapter adapter;
	private String currentPath;//当前选中的文件夹路径
	private List<String> externalPaths;

	@BindView(R.id.tv_file_folder_path)
	TextView tvPath;
	@BindView(R.id.tv_file_folder_tip)
	TextView tvTip;
	@BindView(R.id.lv_file_folder_content)
	ListView lvContent;

	@BindView(R.id.toolbar)
	Toolbar toolbar;
	@BindView(R.id.ll_content)
	RelativeLayout llContent;

	private MoProgressHUD moProgressHUD;

	private Menu aMenu;//获取菜单

	@Override
	protected void onCreateActivity() {
		setContentView(R.layout.activity_file_folder);
	}


	@Override
	protected void initData() {

	}

	@Override
	protected void bindView() {
		ButterKnife.bind(this);
		this.setSupportActionBar(toolbar);
		setupActionBar();
		moProgressHUD = new MoProgressHUD(this);

		refreshRootView();
		//refreshView();
	}



    @Override
    public void searchFinish() {

    }

    @Override
    public void addSuccess() {
		moProgressHUD.dismiss();
		Toast.makeText(this, "添加书籍成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void addError() {

    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case R.id.action_add_book_source:
				startActivityByAnim(new Intent(FileFolderActivity.this, ImportBookActivity.class)
						.putExtra("path",currentPath), 0, 0);
				break;
			case android.R.id.home:
				onBackPressed();
				break;
		}
		return super.onOptionsItemSelected(item);
	}


	// 添加菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_file_selector_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

    public View getView() {
        return llContent;
    }

	/**
	 * 获取根目录
	 */
	private void refreshRootView(){
		tvPath.setVisibility(View.GONE);
		refreshRootData();
		refreshList();
	}

    /**
	 * 刷新界面
	 * */
    private void refreshView() {
		tvPath.setVisibility(View.VISIBLE);
		//刷新列表
		refreshData();
		refreshList();
    }

	private void refreshList(){
		if (adapter == null) {
			adapter = new FileFolderListAdapter(instance, data);
			lvContent.setAdapter(adapter);
			lvContent.setOnItemClickListener(this);
		} else {
			adapter.notifyDataSetChanged();
		}
		//刷新tvTip
		if (adapter.getCount() == 0) {
			tvTip.setVisibility(View.VISIBLE);
		} else {
			tvTip.setVisibility(View.INVISIBLE);
		}
		//刷新tvPath
		tvPath.setText(currentPath);
	}


    @Override
    public void onBackPressed() {
    	boolean isRoot = false;
        //判断是否到达根目录
		if (externalPaths!=null&&currentPath!=null){
			for (int i = 0; i < externalPaths.size(); i++) {
				if (currentPath.equals(externalPaths.get(i))){
					isRoot = true;
				}
			}
		}else{
			super.onBackPressed();//等同于直接finish();
			return;
		}

    	if (!isRoot&&currentPath!=null) {//若未到达根目录，则返回文件上层目录
            File file = new File(currentPath);
            currentPath = file.getParentFile().getAbsolutePath();
            refreshView();
        } else if (currentPath!=null){//到达根目录，展示根目录列表
			tvPath.setVisibility(View.GONE);
			refreshRootView();
			checkOptionMenu(false);
			currentPath = null;
        }
    }

    /**
	 * 文件数据刷新
	 */
	private void refreshData() {
		data.clear();
		List<File> fileList = getFileList(currentPath);
		if (fileList != null) {
			for (File file : fileList) {
				String suffix=file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
				if (file.isDirectory()||suffix.equalsIgnoreCase("txt")){
					MyFile temp = new MyFile();

					temp.file = file;
					data.add(temp);
				}

			}
		}
	}

	/**
	 * 获取根目录数据
	 */
	private void refreshRootData(){
		data.clear();
		externalPaths = SDCardScanner.getStorageData(this);
		if (externalPaths!=null){
			for (int i = 0; i < externalPaths.size(); i++) {
				MyFile temp = new MyFile();
				temp.file = new File(externalPaths.get(i));
				data.add(temp);
			}
		}
	}
	
	/**
	 * 获取当前路径下的文件列表
	 */
	private List<File> getFileList(String path) {
		File file = new File(path);
		if (file.exists()) {
			File[] fileArray = file.listFiles(new EmptyFileFilter());//不显示隐藏文件
			if (fileArray != null && fileArray.length != 0) {//不显示空文件夹，此处不做判断，有些情况会崩溃
				List<File> fileList = Arrays.asList(fileArray);//将File[]转换为List<File>,以便调用Collections.sort进行排序
				Collections.sort(fileList, new FileComparator());//文件排序
				return fileList;
			}
		}
		return null;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MyFile file = data.get(position);
		if (file.file.isDirectory()) {//文件夹
			currentPath = file.file.getAbsolutePath();
			refreshView();
			checkOptionMenu(true);
		} else {
			moProgressHUD.showLoading("放入书架中...");
			List<File> fileList = new ArrayList<>();
			fileList.add(file.file);
			mPresenter.importBooks(fileList);

		}
	}

    @Override
    protected IImportBookPresenter initInjector() {
        return new ImportBookPresenterImpl();
    }

    @Override
	protected void firstRequest() {

	}


	//设置ToolBar
	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(R.string.book_file_selector);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		aMenu = menu;
		for (int i = 0; i < menu.size(); i++){
			menu.getItem(i).setVisible(false);
			menu.getItem(i).setEnabled(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	private void checkOptionMenu(Boolean optionMenuOn){
		if(null != aMenu){
			if(optionMenuOn){
				for (int i = 0; i < aMenu.size(); i++){
					aMenu.getItem(i).setVisible(true);
					aMenu.getItem(i).setEnabled(true);
				}
			}else{
				for (int i = 0; i < aMenu.size(); i++){
					aMenu.getItem(i).setVisible(false);
					aMenu.getItem(i).setEnabled(false);
				}
			}
		}
	}

	@Override
    public void addNewBook(File newFile) {

    }
}

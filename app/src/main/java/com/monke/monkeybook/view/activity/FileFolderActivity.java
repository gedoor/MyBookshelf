package com.monke.monkeybook.view.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.monke.basemvplib.impl.IPresenter;
import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseActivity;
import com.monke.monkeybook.bean.MyFile;
import com.monke.monkeybook.bean.SelectedFiles;
import com.monke.monkeybook.presenter.ImportBookPresenterImpl;
import com.monke.monkeybook.presenter.impl.IImportBookPresenter;
import com.monke.monkeybook.utils.fileselectorutil.EmptyFileFilter;
import com.monke.monkeybook.utils.fileselectorutil.FileComparator;
import com.monke.monkeybook.view.adapter.FileFolderListAdapter;
import com.monke.monkeybook.view.impl.IImportBookView;
import com.monke.monkeybook.widget.modialog.MoProgressHUD;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileFolderActivity extends MBaseActivity<IImportBookPresenter> implements OnItemClickListener,IImportBookView {
	private FileFolderActivity instance = this;
	private ArrayList<MyFile> data = new ArrayList<>();
	private FileFolderListAdapter adapter;
	private String root;//进入文件夹时的根路径，用于判断按返回键时何时返回上一界面以及后退到上一文件夹
	private String currentPath;//当前选中的文件夹路径

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

	private Animation animIn;
	private MoProgressHUD moProgressHUD;
	
	/**
	 * 启动活动的入口方法
	 * */
	public static void actionStart(Context context, String root, int REQUEST) {
		Intent intent = new Intent(context, FileFolderActivity.class); 
		intent.putExtra("root", root);//添加首次加载的根路径
		((Activity)context).startActivityForResult(intent, REQUEST);
	}

	@Override
	protected void onCreateActivity() {
		setContentView(R.layout.activity_file_folder);
	}


	@Override
	protected void initData() {
		animIn = AnimationUtils.loadAnimation(this, R.anim.anim_act_importbook_in);

		root = getIntent().getStringExtra("root");
		currentPath = root;

	}

	@Override
	protected void bindView() {
		ButterKnife.bind(this);
		this.setSupportActionBar(toolbar);
		setupActionBar();
		moProgressHUD = new MoProgressHUD(this);

		refreshView();
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
	 * 刷新界面
	 * */
    private void refreshView() {
		//刷新列表
		refreshData();
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
        if (!currentPath.equals(root)) {//若未到达根目录，则返回文件上层目录
            File file = new File(currentPath);
            currentPath = file.getParentFile().getAbsolutePath();
            refreshView();
        } else {//到达根目录，直接返回
            super.onBackPressed();//等同于直接finish();
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
					if (SelectedFiles.files.containsKey(file.getAbsolutePath())) {//若文件已选中过，则标记为选中
						temp.checked = true;
					}

					if (suffix.equalsIgnoreCase("txt")){
						temp.isTxt = true;
					}
					temp.file = file;
					data.add(temp);
				}

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
		} else {
			if (file.isTxt){
				moProgressHUD.showLoading("放入书架中...");
				List<File> fileList = new ArrayList<>();
				fileList.add(file.file);
				mPresenter.importBooks(fileList);
			}
		}
	}

    @Override
    protected IImportBookPresenter initInjector() {
        return new ImportBookPresenterImpl();
    }

    @Override
	protected void firstRequest() {
		llContent.startAnimation(animIn);
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
    public void addNewBook(File newFile) {

    }
}

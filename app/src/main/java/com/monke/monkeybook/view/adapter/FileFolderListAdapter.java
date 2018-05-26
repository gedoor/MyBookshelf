package com.monke.monkeybook.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.bean.MyFile;
import com.monke.monkeybook.utils.fileSelectorUtil.FileHelper;

import java.util.ArrayList;

public class FileFolderListAdapter extends BaseAdapter {
	private ArrayList<MyFile> data;
	private LayoutInflater inflater;
	private class ViewHolder {
		RelativeLayout rlFolder;
		TextView tvFolderName;
		RelativeLayout rlFile;
		ImageView ivFileChecked;
		ImageView ivFilePreview;
		TextView tvFileName;
	}
	
	public FileFolderListAdapter(Context context, ArrayList<MyFile> data) {
		this.data = data;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return (null == data) ? 0 : data.size();
	}

	@Override
	public MyFile getItem(int position) {
		if (data.get(position) != null) {
			return data.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view  = null;
		ViewHolder holder = null;
		if (convertView == null) {
			view = inflater.inflate(R.layout.adapter_file_folder_item, null);
			holder = new ViewHolder();
			holder.rlFolder = (RelativeLayout) view.findViewById(R.id.rl_file_folder_item_folder);
			holder.tvFolderName = (TextView) view.findViewById(R.id.tv_file_folder_item_folder);
			holder.rlFile = (RelativeLayout) view.findViewById(R.id.rl_file_folder_item_file);
			holder.ivFilePreview = (ImageView) view.findViewById(R.id.iv_file_folder_item_file);
			holder.tvFileName = (TextView) view.findViewById(R.id.tv_file_folder_item_file);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		MyFile file = getItem(position);
		
		if (file != null) {
			if (file.file.isDirectory()) {//file类型为文件夹
				holder.rlFolder.setVisibility(View.VISIBLE);
				holder.rlFile.setVisibility(View.GONE);
				holder.tvFolderName.setText(file.file.getName());
			} else {//file类型为文件
				holder.rlFolder.setVisibility(View.GONE);
				holder.rlFile.setVisibility(View.VISIBLE);
				if (file.checked) {
					//holder.ivFileChecked.setImageResource(R.drawable.ic_checkbox_selected);
				} else {
					//holder.ivFileChecked.setImageResource(R.drawable.ic_checkbox_normal);
				}
				String fileName = file.file.getName();
				holder.tvFileName.setText(fileName);
				holder.ivFilePreview.setImageResource(FileHelper.getImageBySuffix(fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())));
			}
		}
		
		return view;
	}
	
}

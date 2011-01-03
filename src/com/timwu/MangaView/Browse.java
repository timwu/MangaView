package com.timwu.MangaView;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.timwu.MangaView.R;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Browse extends ListActivity implements OnItemClickListener, FilenameFilter {

	private File file;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse);
		if (!isExternalStorageAvailable()) {
			setEmptyText(R.string.sd_unavailable);
			return;
		}
		if (!getFile().isDirectory()) {
			setEmptyText(R.string.non_directory_error);
			return;
		}
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getFile().list(this)));
		getListView().setOnItemClickListener(this);
	}
	
	private void setEmptyText(int resId) {
		TextView tv = (TextView) findViewById(android.R.id.empty);
		tv.setText(resId);
	}
	
	private void setEmptyText(String s) {
		TextView tv = (TextView) findViewById(android.R.id.empty);
		tv.setText(s);
	}
	
	private File getFile() {
		if (file != null) return file;
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			file = new File(getIntent().getData().getPath());
		} else {
			file = Environment.getExternalStorageDirectory();
		}
		return file;
	}
	
	private boolean isExternalStorageAvailable() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state) || 
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {
		String filename = (String) listView.getAdapter().getItem(position);
		File f = new File(file, filename);
		if (f.isDirectory()) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromFile(f), this, this.getClass()));
		}
	}

	@Override
	public boolean accept(File dir, String filename) {
		// hide hidden files.
		return !filename.startsWith(".");
	}
}

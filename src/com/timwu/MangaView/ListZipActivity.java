package com.timwu.MangaView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.timwu.HelloAndroid.R;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class ListZipActivity extends ListActivity implements OnItemClickListener {
	private static final String TAG = "ListZipActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			ZipFile zip = new ZipFile(getIntent().getData().getPath());
			List<String> zipContents = new ArrayList<String>();
			Enumeration<? extends ZipEntry> zipEntries = zip.entries();
			while(zipEntries.hasMoreElements()) {
				zipContents.add(zipEntries.nextElement().getName());
			}
			setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, zipContents));
			getListView().setOnItemClickListener(this);
		} catch (IOException e) {
			Log.e(TAG, "Can't open zip file.", e);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View item, int arg2, long arg3) {
		Intent intent = new Intent();
		intent.setClass(this, ViewPage.class);
		TextView textView = (TextView) item;
		intent.setData(getIntent().getData().buildUpon()
				          .appendQueryParameter("image", textView.getText().toString())
				       .build());
		intent.setAction(Intent.ACTION_VIEW);
		startActivity(intent);
	}
}

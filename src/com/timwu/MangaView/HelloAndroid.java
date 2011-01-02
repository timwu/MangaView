package com.timwu.MangaView;

import java.io.File;

import com.timwu.HelloAndroid.R;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HelloAndroid extends ListActivity implements OnItemClickListener{
	private static final String TAG = "HelloAndroid";
	private File mangaFolder;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getMangaFolder() == null) {
        	Log.e(TAG, "Couldn't open the manga folder.");
        }
    	setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, getMangaFolder().list()));
    	ListView lv = getListView();
    	lv.setOnItemClickListener(this);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Test Item");
		return true;
	}
	
	private File getMangaFolder() {
		if (mangaFolder == null) {
	        String externalStorageState = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(externalStorageState) || 
					Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState)) {
        	mangaFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Manga");
			}
		}
        return mangaFolder;
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent intent = new Intent();
		intent.setClass(this, ListZipActivity.class);
		TextView textView = (TextView) arg1;
		intent.setData(Uri.fromFile(new File(getMangaFolder(), textView.getText().toString())));
		intent.setAction(Intent.ACTION_VIEW);
		startActivity(intent);
	}
}
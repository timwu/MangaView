package com.timwu.MangaView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Read extends Activity {
	private static final String TAG = Read.class.getSimpleName();
	private static final String CURRENT_PAGE_KEY = "currentPage";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read);
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			Log.i(TAG, "Setting up MangaView with volume from " + getIntent().getDataString());
			getMangaView().setVolume(MangaVolume.getMangaVolumeForUri(getIntent().getData()));
		}
		if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_PAGE_KEY)) {
			getMangaView().setPage(savedInstanceState.getInt(CURRENT_PAGE_KEY));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CURRENT_PAGE_KEY, getMangaView().getPage());
	}
	
	private View getMangaView() {
		return (View) findViewById(R.id.read_manga_page_view);
	}
}

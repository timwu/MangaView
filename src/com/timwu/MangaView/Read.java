package com.timwu.MangaView;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Read extends Activity implements FilenameFilter {

	private File file;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			file = new File(getIntent().getData().getPath());
		}
	}
	
	private MangaPageView getMangaPageView() {
		return (MangaPageView) findViewById(R.id.read_manga_page_view);
	}

	@Override
	public boolean accept(File dir, String filename) {
		// Filter for image files
		return filename.endsWith(".png") || 
		       filename.endsWith(".jpg") || 
		       filename.endsWith(".bmp");
	}
}

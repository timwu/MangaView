package com.timwu.MangaView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Read extends Activity {
	
	private MangaVolume vol;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			vol = MangaVolume.getMangaVolumeForUri(getIntent().getData());
		}
		getMangaPageView().setImageDrawable(vol.getPage(0));
	}
	
	private MangaPageView getMangaPageView() {
		return (MangaPageView) findViewById(R.id.read_manga_page_view);
	}
}

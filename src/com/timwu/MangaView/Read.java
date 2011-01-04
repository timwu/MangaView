package com.timwu.MangaView;

import com.timwu.MangaView.MangaPageView.IMangaController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Read extends Activity implements IMangaController {
	
	private MangaVolume vol;
	private int currentPage = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read);
		getMangaPageView().setMangaController(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			vol = MangaVolume.getMangaVolumeForUri(getIntent().getData());
		}
		nextPage();
	}
	
	@Override
	public void nextPage() {
		currentPage = (currentPage + 1) % vol.getNumberOfPages();
		getMangaPageView().setImageDrawable(vol.getPage(currentPage));
	}
	
	@Override
	public void prevPage() {
		currentPage = currentPage == 0 ? vol.getNumberOfPages() : currentPage - 1;
		getMangaPageView().setImageDrawable(vol.getPage(currentPage));
	}
	
	private MangaPageView getMangaPageView() {
		return (MangaPageView) findViewById(R.id.read_manga_page_view);
	}
}

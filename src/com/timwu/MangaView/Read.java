package com.timwu.MangaView;

import com.timwu.MangaView.MangaPageView.IMangaController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class Read extends Activity implements IMangaController {
	private static final String TAG = Read.class.getSimpleName();
	private static final String CURRENT_PAGE_KEY = "currentPage";
	
	private MangaVolume vol;
	private int currentPage = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read);
		getMangaPageView().setMangaController(this);
		if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_PAGE_KEY)) {
			currentPage = savedInstanceState.getInt(CURRENT_PAGE_KEY) - 1;
		}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("prev");
		menu.add("next");
		menu.add("scroll_end");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("prev")) {
			prevPage();
		} else if (item.getTitle().equals("next")) {
			nextPage();
		} else if (item.getTitle().equals("scroll_end")) {
			getMangaPageView().translateAnimation(0, 100);
		}
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CURRENT_PAGE_KEY, currentPage);
	}

	@Override
	public void nextPage() {
		currentPage = (currentPage + 1) % vol.getNumberOfPages();
		getMangaPageView().setImageDrawable(vol.getPage(currentPage));
	}
	
	@Override
	public void prevPage() {
		currentPage = currentPage == 0 ? vol.getNumberOfPages() - 1 : currentPage - 1;
		getMangaPageView().setImageDrawable(vol.getPage(currentPage));
	}
	
	private MangaPageView getMangaPageView() {
		return (MangaPageView) findViewById(R.id.read_manga_page_view);
	}
}

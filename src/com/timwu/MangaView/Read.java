package com.timwu.MangaView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class Read extends Activity {
	private static final String TAG = Read.class.getSimpleName();
	private static final String CURRENT_PAGE_KEY = "currentPage";
	private static final float PAGE_TURN_ZONE = 0.1f;
	
	private MangaVolume vol;
	private GestureDetector gestureDetector;
	private int currentPage = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gestureDetector = new GestureDetector(this, new ReadGestureListener());
		setContentView(R.layout.read);
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
		rightPage();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CURRENT_PAGE_KEY, currentPage);
	}

	public void rightPage() {
		currentPage = (currentPage + 1) % vol.getNumberOfPages();
		getMangaPageView().setPageDrawable(vol.getPage(currentPage));
	}
	
	public void leftPage() {
		currentPage = currentPage == 0 ? vol.getNumberOfPages() - 1 : currentPage - 1;
		getMangaPageView().setPageDrawable(vol.getPage(currentPage));
	}
	
	private MangaPageView getMangaPageView() {
		return (MangaPageView) findViewById(R.id.read_manga_page_view);
	}
	
	private class ReadGestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Display display = getWindowManager().getDefaultDisplay();
			int width = display.getWidth();
			if (e.getRawX() < width * PAGE_TURN_ZONE) {
				leftPage();
				return true;
			} else if (e.getRawX() >= (1 - PAGE_TURN_ZONE) * width) {
				rightPage();
				return true;
			}
			return false;
		}
	}
}

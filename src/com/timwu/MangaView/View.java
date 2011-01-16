package com.timwu.MangaView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class View extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = View.class.getSimpleName();
	
	private MangaVolume vol;
	private int page;
	private int xOff;
	private DrawingThread drawingThread;
	private GestureDetector gestureDetector;
	private Bitmap left, right, center;
	private Boolean redraw = false;
	
	private class DrawingThread extends Thread {
		@Override
		public void run() {
			while(!isInterrupted()) {
				synchronized(redraw) {
					if(!redraw) continue;
					Canvas canvas = null;
					redraw = false;
					try {
						canvas = getHolder().lockCanvas();
						synchronized(getHolder()) {
							doDraw(canvas);
						}
					} finally {
						getHolder().unlockCanvasAndPost(canvas);
					}
				}
			}
		}
		
		private void doDraw(Canvas canvas) {
			if (left != null) {
				canvas.drawBitmap(left, new Rect(0, 0, left.getWidth(), left.getHeight()), 
						new Rect(xOff - getWidth(), 0, xOff, getHeight()), new Paint());
			}
			if (center != null) {
				canvas.drawBitmap(center, new Rect(0, 0, center.getWidth(), center.getHeight()), 
						new Rect(xOff, 0, xOff + getWidth(), getHeight()), new Paint());
			}
			if (right != null) {
				canvas.drawBitmap(right, new Rect(0, 0, right.getWidth(), right.getHeight()), 
						new Rect(getWidth() + xOff, 0, getWidth() + xOff + getWidth(), getHeight()), new Paint());
			}
		}
	}
	
	private class GestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			scrollImage((int) distanceX, (int) distanceY);
			return true;
		}
	}

	public View(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public View(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public View(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		getHolder().addCallback(this);
		drawingThread = new DrawingThread();
		gestureDetector = new GestureDetector(context, new GestureListener());
	}
	
	public void setVolume(MangaVolume vol) {
		this.vol = vol;
		xOff = 0;
		setPage(0);
	}
	
	public void setPage(int page) {
		synchronized (redraw) {
			Log.i(TAG, "Setting page " + page);
			this.page = page;
			left = vol.getPageBitmap(page + 1);
			center = vol.getPageBitmap(page);
			right = vol.getPageBitmap(page - 1);
			redraw = true;
		}
	}
	
	private void shiftPageLeft() {
		synchronized (redraw) {
			page += 1;
			right = center;
			center = left;
			left = vol.getPageBitmap(page + 1);
		}
	}
	
	private void shiftPageRight() {
		synchronized (redraw) {
			page -= 1;
			left = center;
			center = right;
			right = vol.getPageBitmap(page - 1);
		}
	}
	
	private void scrollImage(int dx, int dy) {
		synchronized (redraw) {
			xOff -= dx;
			// Clamp X to not fall off available pages
			if (right == null) xOff = xOff < 0 ? 0 : xOff;
			if (left == null) xOff = xOff > 0 ? 0 : xOff;
			
			// Detect a page turn
			if (xOff >= getWidth()) {
				shiftPageLeft();
				xOff -= getWidth();
			} else if (xOff <= -getWidth()) {
				shiftPageRight();
				xOff += getWidth();
			}
			redraw = true;
		}
	}
	
	public int getPage() {
		return page;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		drawingThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		drawingThread.interrupt();
	}
}

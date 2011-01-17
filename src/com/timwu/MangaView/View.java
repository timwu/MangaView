package com.timwu.MangaView;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class View extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = View.class.getSimpleName();
	private static final float PAGE_TURN_THRESHOLD = 0.2f;
	private static final long ANIMATION_DURATION = 500;
	
	private MangaVolume vol;
	private int page;
	private int xOff;
	private DrawingThread drawingThread;
	private GestureDetector gestureDetector;
	private Bitmap left, right, center;
	private Boolean redraw = false;
	private boolean touchDown = false;
	private boolean pageChangeRequested = false;
	
	private class DrawingThread extends Thread {
		private SimpleAnimation animation;
		
		@Override
		public void run() {
			while(!isInterrupted()) {
				synchronized(redraw) {
					processPages();
					processXOff();
					doAnimation();
					if (redraw) {
						doDraw();
						redraw = false;
					}
				}
			}
		}
		
		private void doDraw() {
			Canvas canvas = null;
			try {
				canvas = getHolder().lockCanvas();
				synchronized(getHolder()) {
					if (left != null) {
						canvas.drawBitmap(left, xOff - getWidth(), 0, null);
					}
					if (center != null) {
						canvas.drawBitmap(center, xOff, 0, null);
					}
					if (right != null) {
						canvas.drawBitmap(right, xOff + getWidth(), 0, null);
					}
				}
			} finally {
				getHolder().unlockCanvasAndPost(canvas);
			}
		}
		
		private void processXOff() {
			// Clamp X to not fall off available pages
			if (right == null) xOff = xOff < 0 ? 0 : xOff;
			if (left == null) xOff = xOff > 0 ? 0 : xOff;
						
			if (!touchDown && animation == null) {
				// Setup the animation to either turn the page, or re-center the page.
				if (xOff < -getWidth() * PAGE_TURN_THRESHOLD) {
					Log.i(TAG, "Flipping to right page. xOff " + xOff);
					animation = new SimpleAnimation(new DecelerateInterpolator(), ANIMATION_DURATION, xOff, -getWidth());
				} else if (xOff > getWidth() * PAGE_TURN_THRESHOLD) {
					Log.i(TAG, "Flipping to left page. xOff " + xOff);
					animation = new SimpleAnimation(new DecelerateInterpolator(), ANIMATION_DURATION, xOff, getWidth());
				} else if (xOff != 0) {
					Log.i(TAG, "Recentering page. xOff " + xOff);
					animation = new SimpleAnimation(new DecelerateInterpolator(), ANIMATION_DURATION, xOff, 0);
				}
			}
		}
		
		private void doAnimation() {
			if (animation == null) return;
			xOff = (int) animation.getCurrent();
			if (animation.isDone()) animation = null;
			redraw = true;
		}
		
		private void cancelAnimation() {
			if (animation == null) return;
			xOff = (int) animation.getCurrent();
			animation = null;
		}
	
		private void processPages() {
			if (pageChangeRequested) {
				left = getScaledPage(page + 1);
				center = getScaledPage(page);
				right = getScaledPage(page - 1);
				pageChangeRequested = false;
			}
			// Detect a page change
			if (xOff >= getWidth()) {
				Log.i(TAG, "shifting page left.");
				shiftPageLeft();
				xOff -= getWidth();
			} else if (xOff <= -getWidth()) {
				Log.i(TAG, "shifting page right.");
				shiftPageRight();
				xOff += getWidth();
			}
		}
	}
	
	private class GestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			synchronized (redraw) {
				touchDown = true;
				drawingThread.cancelAnimation();
			}
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			synchronized (redraw) {
				xOff -= distanceX;
				redraw = true;
			}
			return true;
		}
	}
	
	private class SimpleAnimation {
		private long startTime;
		private long duration;
		private float distance;
		private float from, to;
		private Interpolator interpolator;
		
		private SimpleAnimation(Interpolator interpolator, long duration, float from, float to) {
			this.interpolator = interpolator;
			this.duration = duration;
			this.distance = to - from;
			this.from = from;
			this.to = to;
			this.startTime = Calendar.getInstance().getTimeInMillis();
		}
		
		private float getCurrent() {
			if (isDone()) return to;
			long currentTime = Calendar.getInstance().getTimeInMillis();
			float completion = (currentTime - startTime) / (duration * 1.0f) ;
			return interpolator.getInterpolation(completion) * distance + from;
		}
		
		private boolean isDone() {
			return Calendar.getInstance().getTimeInMillis() >= startTime + duration;
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
		setPage(0);
	}
	
	public void setPage(int newPage) {
		synchronized (redraw) {
			Log.i(TAG, "Setting page to " + newPage);
			page = newPage;
			xOff = 0;
			pageChangeRequested = true;
			redraw = true;
		}
	}
	
	private void shiftPageLeft() {
		synchronized (redraw) {
			page += 1;
			right = center;
			center = left;
			left = getScaledPage(page + 1);
		}
	}
	
	private void shiftPageRight() {
		synchronized (redraw) {
			page -= 1;
			left = center;
			center = right;
			right = getScaledPage(page - 1);
		}
	}
	
	private Bitmap getScaledPage(int page) {
		Bitmap src = vol.getPageBitmap(page);
		if (src == null) return null;
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), src.getConfig());
		bitmap.setDensity(src.getDensity());
		Canvas c = new Canvas(bitmap);
		Matrix m = new Matrix();
		float scale = getWidth() / (1.0f * src.getWidth());
		m.setScale(scale, scale);
		c.drawBitmap(src, m, null);
		return bitmap;
	}
	
	public int getPage() {
		return page;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			// Catch the touch up and update state so the drawingThread knows to animate.
			synchronized (redraw) {
				touchDown = false;
			}
		}
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

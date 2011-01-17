package com.timwu.MangaView;

import java.util.Calendar;

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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class View extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = View.class.getSimpleName();
	private static final float PAGE_TURN_THRESHOLD = 0.125f;
	private static final long ANIMATION_DURATION = 500;
	
	private MangaVolume vol;
	private int page;
	private int xOff;
	private DrawingThread drawingThread;
	private GestureDetector gestureDetector;
	private Bitmap left, right, center;
	private Boolean redraw = false;
	private SimpleAnimation currentAnimation;
	
	private class DrawingThread extends Thread {
		@Override
		public void run() {
			while(!isInterrupted()) {
				synchronized(redraw) {
					if(!redraw) continue;
					Canvas canvas = null;
					try {
						canvas = getHolder().lockCanvas();
						synchronized(getHolder()) {
							doDraw(canvas);
						}
					} finally {
						getHolder().unlockCanvasAndPost(canvas);
					}
					if (currentAnimation == null || currentAnimation.isDone()) {
						// Stop drawing if there's no animation to update.
						redraw = false;
					}
				}
			}
		}
		
		private void doDraw(Canvas canvas) {
			int currentXOffset = xOff;
			if (currentAnimation != null) {
				currentXOffset += currentAnimation.getCurrent();
				if (currentAnimation.isDone()) {
					xOff += currentAnimation.getCurrent();
					currentAnimation = null;
				}

			}
			if (left != null) {
				canvas.drawBitmap(left, new Rect(0, 0, left.getWidth(), left.getHeight()), 
						new Rect(currentXOffset - getWidth(), 0, currentXOffset, getHeight()), new Paint());
			}
			if (center != null) {
				canvas.drawBitmap(center, new Rect(0, 0, center.getWidth(), center.getHeight()), 
						new Rect(currentXOffset, 0, currentXOffset + getWidth(), getHeight()), new Paint());
			}
			if (right != null) {
				canvas.drawBitmap(right, new Rect(0, 0, right.getWidth(), right.getHeight()), 
						new Rect(getWidth() + currentXOffset, 0, getWidth() + currentXOffset + getWidth(), getHeight()), new Paint());
			}
		}
	}
	
	private class GestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			synchronized (redraw) {
				if (currentAnimation != null) {
					// If the user touches the screen mid-animation, stop and clear the animation.
					xOff += currentAnimation.getCurrent();
					currentAnimation = null;
				}
			}
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			synchronized (redraw) {
				xOff -= distanceX;
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
			return true;
		}
	}
	
	private class SimpleAnimation {
		private long startTime;
		private long duration;
		private float factor;
		private Interpolator interpolator;
		
		private SimpleAnimation(Interpolator interpolator, long duration, float factor) {
			this.interpolator = interpolator;
			this.duration = duration;
			this.factor = factor;
			this.startTime = Calendar.getInstance().getTimeInMillis();
		}
		
		private float getCurrent() {
			long currentTime = Calendar.getInstance().getTimeInMillis();
			float completion = (currentTime - startTime) / (duration * 1.0f) ;
			return interpolator.getInterpolation(completion) * factor;
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
	
	public int getPage() {
		return page;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			synchronized (redraw) {
				// Figure out which direction to go to re-center the page
				if (xOff > getWidth() * PAGE_TURN_THRESHOLD) {
					// Flip to left
					Log.i(TAG, "Flipping left");
					currentAnimation = new SimpleAnimation(new DecelerateInterpolator(), ANIMATION_DURATION, getWidth() - xOff);
				} else if (xOff > 0  && xOff < getWidth() * PAGE_TURN_THRESHOLD) {
					// Move right to re-center center page
					Log.i(TAG, "Re-cetnering right");
					currentAnimation = new SimpleAnimation(new DecelerateInterpolator(), ANIMATION_DURATION, -xOff);
				} else if (xOff < -getWidth() * PAGE_TURN_THRESHOLD) {
					// Flip to right
					Log.i(TAG, "Flipping right");
					currentAnimation = new SimpleAnimation(new DecelerateInterpolator(), ANIMATION_DURATION, -getWidth() - xOff);
				} else if (xOff < 0 && xOff > -getWidth() * PAGE_TURN_THRESHOLD) {
					// Move left to re-center center page
					Log.i(TAG, "Recentering left");
					currentAnimation = new SimpleAnimation(new DecelerateInterpolator(), ANIMATION_DURATION, -xOff);
				}
				redraw = true;
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

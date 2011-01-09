package com.timwu.MangaView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ScaleGestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

public class MangaPageView extends ImageView {
	private static final String TAG = MangaPageView.class.getSimpleName();
	private static final float INTERIAL_SCROLL_FACTOR = 0.15f;
	
	private GestureDetector gestureDetector;
	private ScaleGestureDetector scaleGestureDetector;
	
	public MangaPageView(Context context) {
		super(context);
		gestureDetector = new GestureDetector(context, new GestureListener());
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	public MangaPageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		gestureDetector = new GestureDetector(context, new GestureListener());
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
	}

	public MangaPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gestureDetector = new GestureDetector(context, new GestureListener());
		scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
	}
	
	public void setPageDrawable(Drawable page) {
		setImageDrawable(page);
		scrollTo(0, 0);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = scaleGestureDetector.onTouchEvent(event);
		result |= gestureDetector.onTouchEvent(event);
		return result;
	}
	
	private class GestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			clearAnimation();
			return false;
		}
		@Override
		public boolean onFling(MotionEvent initialEvent, MotionEvent finalEvent, float xVelocity,
				float yVelocity) {
			TranslateAnimation ta = new TranslateAnimation(0, 0, xVelocity * INTERIAL_SCROLL_FACTOR, yVelocity * INTERIAL_SCROLL_FACTOR);
			ta.setInterpolator(new DecelerateInterpolator());
			ta.setDuration(1000);
			ta.setFillAfter(true);
			startAnimation(ta);
			return true;
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx,
				float dy) {
			scrollBy((int) dx, (int) dy);
			return true;
		}
	}
	
	private class ScaleListener extends SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			LayoutParams layoutParams = new LayoutParams((int) (getWidth() * detector.getScaleFactor()), 
					                                     (int) (getHeight() * detector.getScaleFactor()));
			setLayoutParams(layoutParams);
			return true;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector arg0) {
			return true;
		}
	}
}

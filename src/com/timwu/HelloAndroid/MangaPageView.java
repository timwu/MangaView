package com.timwu.HelloAndroid;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MangaPageView extends ImageView {
	private static final String TAG = MangaPageView.class.getSimpleName();
	
	private Matrix scaleMatrix = new Matrix();
	private Matrix currentMatrix = new Matrix();
	private float startY, ty, ay;
	private float imageHeight, imageWidth;
	
	public MangaPageView(Context context) {
		super(context);
	}

	public MangaPageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MangaPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		float scaleFactor = w;
		scaleFactor /= getDrawable().getIntrinsicWidth();
		imageHeight = scaleFactor * getDrawable().getIntrinsicHeight();
		imageWidth = w;
		scaleMatrix.reset();
		scaleMatrix.setScale(scaleFactor, scaleFactor);
		setImageMatrix(scaleMatrix);
		ty = 0;
		ay = 0;
		Log.d(TAG, "View heigh " + h);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		switch(event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			ay = ty;
			startY = event.getY();
			Log.d(TAG, "Got some touch down at (" + event.getX() + ", " + event.getY() + ").");
			break;
		case MotionEvent.ACTION_MOVE:
			currentMatrix.set(scaleMatrix);
			float dy = event.getY() - startY;
			if (ay + dy > 0) {
				ty = 0; //at the top
			} else if (ay + dy < getHeight() - imageHeight) {
				ty = getHeight() - imageHeight; //at the bottom
			} else {
				ty = ay + dy;
			}
			currentMatrix.postTranslate(0, ty);
			Log.d(TAG, "Got a move event, delta y " + dy);
			Log.d(TAG, "Total translation " + ty);
			break;
		}
		Log.d(TAG, "Bounding rect for drawable " + getDrawable().getBounds().toShortString());
		setImageMatrix(currentMatrix);
		return true;
	}
}

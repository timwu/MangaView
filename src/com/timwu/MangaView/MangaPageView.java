package com.timwu.MangaView;

import org.metalev.multitouch.controller.MultiTouchController;
import org.metalev.multitouch.controller.MultiTouchController.MultiTouchObjectCanvas;
import org.metalev.multitouch.controller.MultiTouchController.PointInfo;
import org.metalev.multitouch.controller.MultiTouchController.PositionAndScale;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class MangaPageView extends ImageView implements MultiTouchObjectCanvas<MangaPageView>, AnimationListener, OnGestureListener {
	private static final String TAG = MangaPageView.class.getSimpleName();
	private static final float PAGE_TURN_THRESHOLD = 300.0f;
	
	private IMangaController controller;
	private MultiTouchController<MangaPageView> multiTouchController = new MultiTouchController<MangaPageView>(this);
	private GestureDetector gestureDetector;
	private Matrix currentMatrix = new Matrix();
	private float scale, minScale; //minScale clamps the scaling to fit the width of the screen at least.
	private float animationEndX, animationEndY;
	
	public MangaPageView(Context context) {
		super(context);
		gestureDetector = new GestureDetector(context, this);
	}

	public MangaPageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		gestureDetector = new GestureDetector(context, this);
	}

	public MangaPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gestureDetector = new GestureDetector(context, this);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		resetImageScale();
	}
	
	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		resetImageScale();
	}

	private void resetImageScale() {
		minScale = scale = (getWidth() * 1.0f) / getDrawable().getIntrinsicWidth();
		currentMatrix.setScale(scale, scale);
		setImageMatrix(currentMatrix);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = multiTouchController.onTouchEvent(event);
		result |= gestureDetector.onTouchEvent(event);
		return result;
	}
	
	public void setMangaController(IMangaController controller) {
		this.controller = controller;
	}
	
	public interface IMangaController {
		public void nextPage();
		public void prevPage();
	}

	@Override
	public MangaPageView getDraggableObjectAtPoint(PointInfo touchPoint) {
		return this;
	}

	@Override
	public void getPositionAndScale(MangaPageView obj,
			PositionAndScale objPosAndScaleOut) {
		float offsets[] = getOffsets();
		objPosAndScaleOut.set(offsets[0], offsets[1], true, scale, false, 0.0f, 0.0f, false, 0.0f);
	}
	
	private float[] getOffsets() {
		float [] offsets = { 0.0f, 0.0f };
		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(scale, scale);
		Matrix scaleInverse = new Matrix();
		scaleMatrix.invert(scaleInverse);
		scaleInverse.postConcat(getImageMatrix());
		scaleInverse.mapPoints(offsets);
		return offsets;
	}

	@Override
	public boolean setPositionAndScale(MangaPageView obj,
			PositionAndScale newObjPosAndScale, PointInfo touchPoint) {
		scale = newObjPosAndScale.getScale() >= minScale ? newObjPosAndScale.getScale() : scale ;
		float yOff = clip(getHeight(), scale * getDrawable().getIntrinsicHeight(), newObjPosAndScale.getYOff());
		float xOff = clip(getWidth(), scale * getDrawable().getIntrinsicWidth(), newObjPosAndScale.getXOff());
		
		// Create and apply the transformation matrix
		// Looks like the matrix multiplication happens this way A * M = B
		currentMatrix.reset();
		currentMatrix.postScale(scale, scale);
		currentMatrix.postTranslate(xOff, yOff);
		setImageMatrix(currentMatrix);
//		if (xOff > pageTurnThreshold) {
//			controller.prevPage();
//			resetImageScale();
//		} else if (xOff < -pageTurnThreshold) {
//			controller.nextPage();
//			resetImageScale();
//		}
		return true;
	}
	
	public void translateAnimation(float dx, float dy) {
		animationEndX = dx;
		animationEndY = dy;
		TranslateAnimation ta = new TranslateAnimation(0, dx, 0, dy);
		ta.setDuration(2000);
		ta.setInterpolator(new DecelerateInterpolator());
		ta.setAnimationListener(this);
		startAnimation(ta);
	}

	private float clip(float boxLen, float imageLen, float offset) {
		if (boxLen < imageLen) {
			if (offset > 0) return 0;
			if (offset < boxLen - imageLen) return boxLen - imageLen;
			return offset;
		} else {
			if (offset > boxLen - imageLen) return boxLen - imageLen;
			if (offset < 0) return 0;
			return offset;
		}
	}

	@Override
	public void selectObject(MangaPageView obj, PointInfo touchPoint) {
		// Ignore
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		Matrix transformMatrix = new Matrix(getImageMatrix());
		transformMatrix.postTranslate(animationEndX, animationEndY);
		setImageMatrix(transformMatrix);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent initialEvent, MotionEvent finalEvent, float xVelocity,
			float yVelocity) {
		if (xVelocity > PAGE_TURN_THRESHOLD) {
			controller.prevPage();
			return true;
		} else if (xVelocity < -PAGE_TURN_THRESHOLD){
			controller.nextPage();
			return true;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		return false;
	}
}

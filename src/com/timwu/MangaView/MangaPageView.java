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
import android.view.MotionEvent;
import android.widget.ImageView;

public class MangaPageView extends ImageView implements MultiTouchObjectCanvas<MangaPageView> {
	private static final String TAG = MangaPageView.class.getSimpleName();
	private static final float PAGE_TURN_THRESHOLD_RATIO = 0.125f;
	
	private IMangaController controller;
	private MultiTouchController<MangaPageView> multiTouchController = new MultiTouchController<MangaPageView>(this);
	private Matrix currentMatrix = new Matrix();
	private float scale, minScale; //minScale clamps the scaling to fit the width of the screen at least.
	private float xOff, yOff, pageTurnThreshold;
	
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
		resetImageScale();
	}
	
	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		resetImageScale();
	}

	private void resetImageScale() {
		xOff = 0;
		yOff = 0;
		minScale = scale = (getWidth() * 1.0f) / getDrawable().getIntrinsicWidth();
		pageTurnThreshold = PAGE_TURN_THRESHOLD_RATIO * getDrawable().getIntrinsicWidth();
		currentMatrix.setScale(scale, scale);
		setImageMatrix(currentMatrix);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return multiTouchController.onTouchEvent(event);
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
		objPosAndScaleOut.set(xOff, yOff, true, scale, false, 0.0f, 0.0f, false, 0.0f);
	}

	@Override
	public boolean setPositionAndScale(MangaPageView obj,
			PositionAndScale newObjPosAndScale, PointInfo touchPoint) {
		scale = newObjPosAndScale.getScale() >= minScale ? newObjPosAndScale.getScale() : scale ;
		yOff = clip(getHeight(), scale * getDrawable().getIntrinsicHeight(), newObjPosAndScale.getYOff());
		xOff = clip(getWidth(), scale * getDrawable().getIntrinsicWidth(), newObjPosAndScale.getXOff());
		
		// Create and apply the transformation matrix
		// Looks like the matrix multiplication happens this way A * M = B
		currentMatrix.reset();
		currentMatrix.postScale(scale, scale);
		currentMatrix.postTranslate(xOff, yOff);
		setImageMatrix(currentMatrix);
		Log.i(TAG, "xOff " + xOff + " threshold " + pageTurnThreshold);
//		if (xOff > pageTurnThreshold) {
//			controller.prevPage();
//			resetImageScale();
//		} else if (xOff < -pageTurnThreshold) {
//			controller.nextPage();
//			resetImageScale();
//		}
		return true;
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
}

package com.timwu.HelloAndroid;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class ViewPage extends Activity implements OnTouchListener {
	private static final String TAG = "ViewPage";
	
	private ImageView iv;
	private Matrix savedMatrix = new Matrix();
	private Matrix currentMatrix = new Matrix();
	private float startY;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		iv = new ImageView(this);
		iv.setScaleType(ImageView.ScaleType.MATRIX);
		iv.setOnTouchListener(this);
		try {
			ZipFile zip = new ZipFile(getIntent().getData().getPath());
			String imageFileName = getIntent().getData().getQueryParameter("image");
			InputStream is = zip.getInputStream(zip.getEntry(imageFileName));
			iv.setImageDrawable(Drawable.createFromStream(is, imageFileName));
		} catch (IOException e) {
			Log.e(TAG, "Failed to open zip file.");
		}
		setContentView(iv);
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent ev) {
		ImageView imageView = (ImageView) view;
		switch(ev.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(currentMatrix);
			startY = ev.getY();
			Log.d(TAG, "Got some touch down at (" + ev.getX() + ", " + ev.getY() + ").");
			break;
		case MotionEvent.ACTION_MOVE:
			currentMatrix.set(savedMatrix);
			currentMatrix.postTranslate(0, ev.getY() - startY);
			Log.d(TAG, "Got a move event, delta y " + (ev.getY() - startY));
			break;
		}
		imageView.setImageMatrix(currentMatrix);
		return true;
	}
}

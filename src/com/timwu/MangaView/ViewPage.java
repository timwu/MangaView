package com.timwu.MangaView;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

import com.timwu.HelloAndroid.R;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class ViewPage extends Activity {
	private static final String TAG = "ViewPage";
	
	private ImageView iv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_page);
		iv = (ImageView) findViewById(R.id.view_page_view);
		try {
			ZipFile zip = new ZipFile(getIntent().getData().getPath());
			String imageFileName = getIntent().getData().getQueryParameter("image");
			InputStream is = zip.getInputStream(zip.getEntry(imageFileName));
			iv.setImageDrawable(Drawable.createFromStream(is, imageFileName));
			Log.d(TAG, "Bounding rect for drawable " + iv.getDrawable().getBounds().toShortString());
		} catch (IOException e) {
			Log.e(TAG, "Failed to open zip file.");
		}
	}
}

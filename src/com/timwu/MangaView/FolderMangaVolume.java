package com.timwu.MangaView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class FolderMangaVolume extends MangaVolume implements FilenameFilter {
	
	private File folder;
	
	public FolderMangaVolume(Uri uri) {
		super(uri);
		folder = new File(uri.getPath());
		pageNames = folder.list(this);
		Arrays.sort(pageNames);
	}

	@Override
	public Bitmap getPageBitmap(int page) {
		if (page < 0 || page >= getNumberOfPages()) return null;
		return BitmapFactory.decodeFile(new File(folder, pageNames[page]).getAbsolutePath());
	}

	@Override
	public boolean accept(File dir, String filename) {
		return validMangaPageFilename(filename);
	}

	public static boolean isValidMangaFolder(File file) {
		return file.isDirectory() && new FolderMangaVolume(Uri.fromFile(file)).getNumberOfPages() > 0;
	}
}

package com.timwu.MangaView;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public abstract class MangaVolume {
	protected Uri uri;
	protected String[] pageNames;
	
	protected MangaVolume(Uri uri) {
		this. uri = uri;
	}
	
	public abstract Bitmap getPageBitmap(int page);
	
	public String[] getPageNames() {
		return pageNames;
	}
	
	public int getNumberOfPages() {
		return pageNames.length;
	}
	
	public static MangaVolume getMangaVolumeForUri(Uri uri) {
		File file = new File(uri.getPath());
		if (file.isDirectory()) {
			return new FolderMangaVolume(uri);
		} else if (file.getName().endsWith("zip") || file.getName().endsWith("cbz")){
			return new ZipMangaVolume(uri);
		} else {
			return null;
		}
	}
	
	public static final boolean validMangaPageFilename(String filename) {
		return filename.endsWith(".jpg") ||
		       filename.endsWith(".png") ||
		       filename.endsWith(".bmp");
	}
	
	public static final boolean validMangaFile(File file) {
		return FolderMangaVolume.isValidMangaFolder(file) ||
		       ZipMangaVolume.isValidMangaZip(file);
	}
}

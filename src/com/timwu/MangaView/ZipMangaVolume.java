package com.timwu.MangaView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

public class ZipMangaVolume extends MangaVolume {
	private static final String TAG = ZipMangaVolume.class.getSimpleName();
	
	private ZipFile zip;
	
	public ZipMangaVolume(Uri uri) {
		super(uri);
		try {
			zip = new ZipFile(uri.getPath());
		} catch (IOException e) {
			Log.e(TAG, "Failed to open zip file at: " + uri.getPath(), e);
		}
		List<String> pageList = new ArrayList<String>();
		Enumeration<? extends ZipEntry> zipEnumeration = zip.entries();
		while(zipEnumeration.hasMoreElements()) {
			ZipEntry zipEntry = zipEnumeration.nextElement();
			if (validMangaPageFilename(zipEntry.getName())) {
				pageList.add(zipEntry.getName());
			}
		}
		pageNames = pageList.toArray(new String[pageList.size()]);
		Arrays.sort(pageNames);
	}
	
	@Override
	public Drawable getPage(int page) {
		if (page < 0 || page >= getNumberOfPages()) return null;
		try {
			return Drawable.createFromStream(zip.getInputStream(zip.getEntry(pageNames[page])), pageNames[page]);
		} catch (IOException e) {
			Log.e(TAG, "Failed to get page " + pageNames[page] + " from the zip file.", e);
			return null;
		}
	}

	public static boolean isValidMangaZip(File file) {
		return file.getName().endsWith(".cbz") || file.getName().endsWith(".zip");
	}
}

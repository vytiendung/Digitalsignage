package com.five9.admin.digitalsignage.Common;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.five9.admin.digitalsignage.MyApplication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Dinh on 1/6/2016.
 */
public class DocumentFileUtil {
	private static final String TAG = DocumentFileUtil.class.getSimpleName();

	private static HashMap<String, DocumentFile> cachedDocumentFile = new HashMap<>();

	private DocumentFileUtil() {
	}

	public static String getExtSdCardFolder() {
		String[] extSdPaths = getExtSdCardPaths();
		Log.d(TAG, "getExtSdCardFolder: " + extSdPaths.length);
		for (int i = 0; i < extSdPaths.length; i++) {
			Log.d(TAG, "getExtSdCardFolder: " + extSdPaths[i]);
		}
		for (int i = 0; i < extSdPaths.length; i++) {
			File f = new File(extSdPaths[i]);
			if (f.exists())
				return extSdPaths[i];
		}
		return null;
	}

	public static String getExtSdCardFolder(final String filePath, Context context) {
		String[] extSdPaths = getExtSdCardPaths();
		for (int i = 0; i < extSdPaths.length; i++) {
			File f = new File(extSdPaths[i]);
			if (f.exists())
				return extSdPaths[i];
//			if (filePath.startsWith(extSdPaths[i])) {
//				return extSdPaths[i];
//			}
		}
		return null;
	}

	public static String getSdCardPath() {
		String sdCardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

		try {
			sdCardDirectory = new File(sdCardDirectory).getCanonicalPath();
		} catch (IOException ioe) {
			Log.d(TAG, "Could not get SD directory", ioe);
		}
		return sdCardDirectory;
	}

	public static String[] getExtSdCardPaths() {
		List<String> paths = new ArrayList<>();
		for (File file : MyApplication.getIntance().getExternalFilesDirs("external")) {
			if (file != null && !file.equals(MyApplication.getIntance().getExternalFilesDir
					("external"))) {
				int index = file.getAbsolutePath().lastIndexOf("/Android/data");
				if (index < 0) {
					Log.d(TAG, "Unexpected external file dir: " + file.getAbsolutePath());
				} else {
					String path = getOneExternalPath(file, index);
					paths.add(path);
				}
			}
		}
		return paths.toArray(new String[paths.size()]);
	}

	@NonNull
	private static String getOneExternalPath(File file, int index) {
		String path = file.getAbsolutePath().substring(0, index);
		try {
			path = new File(path).getCanonicalPath();
		} catch (IOException e) {
			Log.e(TAG, "getExtSdCardPaths: " + e);
			// Keep non-canonical path.
		}
		return path;
	}
}
